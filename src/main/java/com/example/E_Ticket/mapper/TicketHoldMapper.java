package com.example.E_Ticket.mapper;
import com.example.E_Ticket.dto.TicketHoldDto;
import com.example.E_Ticket.entity.TicketHold;

public class TicketHoldMapper {
    public static TicketHoldDto toDto(TicketHold h){
        return new TicketHoldDto(
                h.getId(),
                h.getEvent().getId(),
                h.getTicketType().getId(),
                h.getQty(),
                h.getStatus().name(),
                h.getExpiresAt()
        );
    }
}