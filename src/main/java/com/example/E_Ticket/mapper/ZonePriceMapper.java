package com.example.E_Ticket.mapper;

import com.example.E_Ticket.dto.ZonePriceDto;
import com.example.E_Ticket.entity.ZonePrice;

public class ZonePriceMapper {
    public static ZonePriceDto toDto(ZonePrice zp){
        return new ZonePriceDto(
                zp.getId(),
                zp.getEvent().getId(),
                zp.getTicketType().getId(),
                zp.getSeatZone().getId(),
                zp.getPrice()
        );
    }
}
