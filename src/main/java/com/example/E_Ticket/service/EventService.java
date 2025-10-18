package com.example.E_Ticket.service;

import com.example.E_Ticket.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EventService {
    Page<Event> list(String q, Pageable pageable);
    Event getById(Long id);
    Event getBySlug(String slug);
    Event create(Event event);
    Event update(Long id, Event patch);
    void delete(Long id);
}
