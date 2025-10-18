package com.example.E_Ticket.mapper;

import com.example.E_Ticket.dto.SeatMapDto;
import com.example.E_Ticket.entity.SeatMap;

public class SeatMapMapper {
    public static SeatMapDto toDto(SeatMap m){
        return new SeatMapDto(
                m.getId(),
                m.getVenue().getId(),
                m.getName(),
                m.getSvgPath()
        );
    }
}
