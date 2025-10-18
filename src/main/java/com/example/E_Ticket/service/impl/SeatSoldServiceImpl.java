package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.entity.SoldSeat;
import com.example.E_Ticket.repository.SeatZoneRepository;
import com.example.E_Ticket.repository.SoldSeatRepository;
import com.example.E_Ticket.service.SeatSoldService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SeatSoldServiceImpl implements SeatSoldService {
    private final InventorySeatHoldService inMemHold;
    private final SoldSeatRepository soldSeatRepo;
    private final SeatZoneRepository zoneRepo;

    @Override
    @Transactional
    public void commitSeatsForOrder(Order order, String sessionId) {
        var list = inMemHold.snapshotByEventAndSession(order.getEvent().getId(), sessionId);
        if (list.isEmpty()) return;

        for (var h : list) {
            if (soldSeatRepo.existsByEvent_IdAndZone_IdAndSeatNo(h.getEventId(), h.getZoneId(), h.getSeatNo()))
                continue;

            var zone = zoneRepo.findById(h.getZoneId()).orElseThrow();
            var row = SoldSeat.builder()
                    .event(order.getEvent())
                    .zone(zone)
                    .seatNo(h.getSeatNo())
                    .order(order)
                    .build();
            soldSeatRepo.save(row);

            inMemHold.clear(h.getEventId(), h.getZoneId(), h.getSeatNo());
        }
    }
}
