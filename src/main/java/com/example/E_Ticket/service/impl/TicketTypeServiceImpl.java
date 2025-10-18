package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.entity.Event;
import com.example.E_Ticket.entity.TicketType;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.repository.EventRepository;
import com.example.E_Ticket.repository.TicketTypeRepository;
import com.example.E_Ticket.service.TicketTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class TicketTypeServiceImpl implements TicketTypeService {
    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;

    @Override
    public List<TicketType> listByEvent(Long eventId) {
        return ticketTypeRepository.findByEventId(eventId);
    }

    @Override
    public TicketType getById(Long id) {
        return ticketTypeRepository.findById(id).orElseThrow(()-> new NotFoundException("TicketType Not Found"));
    }

    @Override
    public TicketType create(Long eventId, TicketType ticketType) {
        Event e = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));
        ticketType.setEvent(e);
        if (ticketType.getSold()==null) ticketType.setSold(0);
        return ticketTypeRepository.save(ticketType);
    }

    @Override
    public TicketType update(Long id, Long eventId, TicketType patch) {
        TicketType t = getById(id);
        if (eventId!=null){
            Event e = eventRepository.findById(eventId).orElseThrow(() -> new NotFoundException("Event not found"));
            t.setEvent(e);
        }
        if (patch.getName()!=null) t.setName(patch.getName());
        if (patch.getPrice()!=null) t.setPrice(patch.getPrice());
        if (patch.getQuota()!=null) t.setQuota(patch.getQuota());
        if (patch.getSold()!=null) t.setSold(patch.getSold());
        return t;
    }

    @Override
    public void delete(Long id) {
        ticketTypeRepository.deleteById(id);
    }

}
