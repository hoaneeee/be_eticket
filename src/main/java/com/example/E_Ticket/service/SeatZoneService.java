package com.example.E_Ticket.service;

import com.example.E_Ticket.dto.SeatZoneDto;
import com.example.E_Ticket.dto.SeatZoneUpsertReq;

import java.util.List;

public interface SeatZoneService {
    List<SeatZoneDto> listByMap(Long seatMapId);
    SeatZoneDto create(Long seatMapId, SeatZoneUpsertReq req);
    SeatZoneDto update(Long zoneId, SeatZoneUpsertReq req);
    void delete(Long zoneId);
}
