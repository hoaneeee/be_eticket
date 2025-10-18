package com.example.E_Ticket.mapper;

import com.example.E_Ticket.dto.CheckinDto;
import com.example.E_Ticket.entity.CheckIn;

public class CheckinMapper {
    public static CheckinDto toDto(CheckIn c){
        return new CheckinDto(
                c.getId(), c.getCode(),
                c.getOrder() != null ? c.getOrder().getId() : null,
                c.getScannedBy(), c.getScannedAt()
        );
    }
}
