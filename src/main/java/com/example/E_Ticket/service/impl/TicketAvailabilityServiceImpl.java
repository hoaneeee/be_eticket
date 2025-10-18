// TicketAvailabilityServiceImpl.java
package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.entity.Event;
import com.example.E_Ticket.entity.SeatZone;
import com.example.E_Ticket.entity.SoldSeat;
import com.example.E_Ticket.entity.TicketType;
import com.example.E_Ticket.entity.ZonePrice;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.repository.*;
import com.example.E_Ticket.service.TicketAvailabilityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TicketAvailabilityServiceImpl implements TicketAvailabilityService {

    private final EventRepository eventRepo;
    private final TicketTypeRepository ticketTypeRepo;
    private final SeatZoneRepository seatZoneRepo;
    private final ZonePriceRepository zonePriceRepo;
    private final SoldSeatRepository soldSeatRepo;
    private final InventorySeatHoldService holdSvc;

    @Override
    public List<Item> availableByTicketType(Long eventId) {
        Event e = eventRepo.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event not found"));

        // 1) Event 0 có seat map -> quota - sold
        if (e.getSeatMap() == null) {
            List<TicketType> types = ticketTypeRepo.findByEventId(eventId);
            return types.stream().map(tt -> {
                Integer quota = tt.getQuota();
                Integer sold  = Optional.ofNullable(tt.getSold()).orElse(0);
                int available = (quota == null) ? Integer.MAX_VALUE : Math.max(0, quota - sold);
                return new Item(tt.getId(), tt.getName(), available);
            }).toList();
        }

        // 2) Event co seat map -> gop theo ticket type qua ZonePrice
        Long mapId = e.getSeatMap().getId();

        // capacity theo zone
        Map<Long, Integer> capByZone = seatZoneRepo.findBySeatMap_Id(mapId).stream()
                .collect(Collectors.toMap(SeatZone::getId, z -> Optional.ofNullable(z.getCapacity()).orElse(0)));

        // sold theo zone
        Map<Long, Long> soldByZone = soldSeatRepo.findByEvent_Id(eventId).stream()
                .collect(Collectors.groupingBy(s -> s.getZone().getId(), Collectors.counting()));

        // holds theo zone
        Map<Long, Long> holdByZone = holdSvc.snapshotByEvent(eventId).stream()
                .collect(Collectors.groupingBy(InventorySeatHoldService.Hold::getZoneId, Collectors.counting()));

        // duyệt ZonePrice để biết zone -> ticketType
        List<ZonePrice> zps = zonePriceRepo.findByEvent_Id(eventId);

        Map<Long, Integer> availableByTt = new HashMap<>(); // ticketTypeId -> available
        Map<Long, String>  ttName        = new HashMap<>();

        for (ZonePrice zp : zps) {
            Long zoneId = zp.getSeatZone().getId();
            Long ttId   = zp.getTicketType().getId();

            int cap  = capByZone.getOrDefault(zoneId, 0);
            int sold = soldByZone.getOrDefault(zoneId, 0L).intValue();
            int hold = holdByZone.getOrDefault(zoneId, 0L).intValue();

            int left = Math.max(0, cap - sold - hold);
            if (left <= 0) {
                // không còn ghế trong zone này
                availableByTt.putIfAbsent(ttId, 0);
            } else {
                availableByTt.merge(ttId, left, Integer::sum);
            }
            ttName.put(ttId, zp.getTicketType().getName());
        }

        // bảo đảm có cả những ticket type không gắn zone
        for (TicketType tt : ticketTypeRepo.findByEventId(eventId)) {
            availableByTt.putIfAbsent(tt.getId(), 0);
            ttName.putIfAbsent(tt.getId(), tt.getName());
        }

        return availableByTt.entrySet().stream()
                .map(e2 -> new Item(e2.getKey(), ttName.getOrDefault(e2.getKey(), "—"), e2.getValue()))
                .sorted(Comparator.comparing(Item::ticketTypeName))
                .toList();
    }
}
