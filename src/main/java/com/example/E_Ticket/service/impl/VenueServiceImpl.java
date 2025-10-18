package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.dto.VenueDto;
import com.example.E_Ticket.dto.VenueUpsertReq;
import com.example.E_Ticket.entity.Venue;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.mapper.VenueMapper;
import com.example.E_Ticket.repository.EventRepository;
import com.example.E_Ticket.repository.VenueRepository;
import com.example.E_Ticket.service.VenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VenueServiceImpl implements VenueService {
    private final VenueRepository venueRepo;
    private final EventRepository eventRepo;

    @Override
    public Page<VenueDto> list(int page, int size) {
        return venueRepo.findAll(PageRequest.of(page, size, Sort.by("id").descending()))
                .map(VenueMapper::toDto);
    }

    @Override
    public VenueDto get(Long id) {
        Venue v = venueRepo.findById(id).orElseThrow(() -> new NotFoundException("Venue not found"));
        return VenueMapper.toDto(v);
    }

    @Override
    public VenueDto create(VenueUpsertReq req) {
        return VenueMapper.toDto(venueRepo.save(VenueMapper.fromReq(req)));
    }

    @Override
    public VenueDto update(Long id, VenueUpsertReq req) {
        Venue v = venueRepo.findById(id).orElseThrow(() -> new NotFoundException("Venue not found"));
        VenueMapper.patch(v, req);
        return VenueMapper.toDto(venueRepo.save(v));
    }

    @Override
    public void delete(Long id) {
        if (eventRepo.existsByVenue_Id(id))
            throw new RuntimeException("Cannot delete venue: used by events");
        venueRepo.deleteById(id);
    }
}
