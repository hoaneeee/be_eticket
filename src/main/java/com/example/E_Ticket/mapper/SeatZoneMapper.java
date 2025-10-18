package com.example.E_Ticket.mapper;

import com.example.E_Ticket.dto.SeatZoneDto;
import com.example.E_Ticket.entity.SeatZone;

public class SeatZoneMapper {
    public static SeatZoneDto toDto(SeatZone z){
        return new SeatZoneDto(
                z.getId(),
                z.getSeatMap().getId(),
                z.getCode(),
                z.getName(),
                z.getCapacity(),
                z.getPolygon()
        );
    }
}
