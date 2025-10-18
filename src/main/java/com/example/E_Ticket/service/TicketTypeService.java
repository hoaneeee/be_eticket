package com.example.E_Ticket.service;

import com.example.E_Ticket.entity.TicketType;

import java.util.List;

public interface TicketTypeService {
    List<TicketType> listByEvent( Long eventId);
    TicketType getById(Long id);
    TicketType create(Long eventId, TicketType ticketType);
    TicketType update(Long id, Long eventId, TicketType patch);
    void delete(Long id);
}
