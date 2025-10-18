package com.example.E_Ticket.service;

import com.example.E_Ticket.dto.ZonePriceDto;
import com.example.E_Ticket.dto.ZonePriceUpsertReq;

import java.util.List;

public interface ZonePriceService {
    List<ZonePriceDto> listByEvent(Long eventId);
    ZonePriceDto upsert(ZonePriceUpsertReq req);
    void delete(Long id);
}
