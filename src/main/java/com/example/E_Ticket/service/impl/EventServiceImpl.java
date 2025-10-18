package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.entity.Event;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.repository.EventRepository;
import com.example.E_Ticket.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;

    @Override
    public Page<Event> list(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return eventRepository.findAll(pageable);
        }
        return eventRepository.search(q, pageable);
    }

    @Override
    public Event getById(Long id) {
        return eventRepository.findById(id).orElseThrow(()-> new NotFoundException("Event Not Found"));
    }

    @Override
    public Event getBySlug(String slug) {
        return eventRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Event not found"));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Event create(Event req) {
        if (req.getSlug() != null && eventRepository.existsBySlug(req.getSlug())) {
            throw new IllegalArgumentException("Slug đã tồn tại");
        }
        return eventRepository.save(req);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Event update(Long id, Event patch) {
        Event e = getById(id);
        applyPatch(e, patch);
        if (e.getSlug() != null && eventRepository.existsBySlugAndIdNot(e.getSlug(), e.getId())) {
            throw new IllegalArgumentException("Slug đã tồn tại");
        }
        return e;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        eventRepository.delete(getById(id));
    }

    private void applyPatch(Event e, Event patch) {
        if (patch.getTitle() != null)       e.setTitle(patch.getTitle());
        if (patch.getSlug() != null)        e.setSlug(patch.getSlug());
        if (patch.getEventDate() != null)   e.setEventDate(patch.getEventDate());
        if (patch.getVenue() != null)       e.setVenue(patch.getVenue());
        if (patch.getDescription() != null) e.setDescription(patch.getDescription());
        if (patch.getBannerUrl() != null)   e.setBannerUrl(patch.getBannerUrl());
        if (patch.getStatus() != null)      e.setStatus(patch.getStatus());
    }
}
