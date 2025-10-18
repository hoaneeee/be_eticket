package com.example.E_Ticket.mapper;

import com.example.E_Ticket.dto.RefundDto;
import com.example.E_Ticket.entity.Refund;

public class RefundMapper {
    public static RefundDto toDto(Refund r){
        return new RefundDto(
                r.getId(), r.getOrder().getId(), r.getAmount(), r.getReason(),
                r.getStatus().name(), r.getNote(), r.getCreatedBy(), r.getCreatedAt()
        );
    }
}