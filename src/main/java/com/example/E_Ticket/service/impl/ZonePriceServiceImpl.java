package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.dto.ZonePriceDto;
import com.example.E_Ticket.dto.ZonePriceUpsertReq;
import com.example.E_Ticket.entity.ZonePrice;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.mapper.ZonePriceMapper;
import com.example.E_Ticket.repository.*;
import com.example.E_Ticket.service.ZonePriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ZonePriceServiceImpl implements ZonePriceService {

    private final ZonePriceRepository repo;
    private final EventRepository eventRepo;
    private final TicketTypeRepository ticketRepo;
    private final SeatZoneRepository zoneRepo;

    @Override
    @Transactional(readOnly = true)
    public List<ZonePriceDto> listByEvent(Long eventId) {
        return repo.findByEvent_Id(eventId).stream().map(ZonePriceMapper::toDto).toList();
    }

    @Override
    @Transactional
    public ZonePriceDto upsert(ZonePriceUpsertReq r) {
        var event = eventRepo.findById(r.eventId()).orElseThrow(() -> new NotFoundException("Event not found"));
        var tt    = ticketRepo.findById(r.ticketTypeId()).orElseThrow(() -> new NotFoundException("Ticket type not found"));
        var zone  = zoneRepo.findById(r.seatZoneId()).orElseThrow(() -> new NotFoundException("Seat zone not found"));

        var ex = repo.findByEvent_IdAndTicketType_IdAndSeatZone_Id(r.eventId(), r.ticketTypeId(), r.seatZoneId());
        ZonePrice zp = ex.orElseGet(ZonePrice::new);
        zp.setEvent(event); zp.setTicketType(tt); zp.setSeatZone(zone); zp.setPrice(r.price());
        return ZonePriceMapper.toDto(repo.save(zp));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repo.deleteById(id);
    }
}
