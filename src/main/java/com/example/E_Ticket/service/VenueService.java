package com.example.E_Ticket.service;

import com.example.E_Ticket.dto.VenueDto;
import com.example.E_Ticket.dto.VenueUpsertReq;
import org.springframework.data.domain.Page;

public interface VenueService {
    Page<VenueDto> list(int page, int size);
    VenueDto get(Long id);
    VenueDto create(VenueUpsertReq req);
    VenueDto update(Long id, VenueUpsertReq req);
    void delete(Long id);
}
