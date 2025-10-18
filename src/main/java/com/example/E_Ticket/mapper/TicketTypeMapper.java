package com.example.E_Ticket.mapper;

import com.example.E_Ticket.dto.TicketTypeDto;
import com.example.E_Ticket.dto.TicketTypeUpsertReq;
import com.example.E_Ticket.entity.Event;
import com.example.E_Ticket.entity.TicketType;

public class TicketTypeMapper {
    public static TicketTypeDto toDto(TicketType t) {
        return new TicketTypeDto(
                t.getId(),
                t.getEvent() != null ? t.getEvent().getId() : null,
                t.getName(), t.getPrice(), t.getQuota(), t.getSold()
        );
    }
    public static TicketType toEntity(TicketTypeUpsertReq r, Event event) {
        TicketType t = new TicketType();
        t.setEvent(event);
        t.setName(r.name());
        t.setPrice(r.price());
        t.setQuota(r.quota());
        t.setSold(r.sold() == null ? 0 : r.sold());
        return t;
    }
    public static void copyToExisting(TicketType t, TicketTypeUpsertReq r, Event event){
        t.setEvent(event);
        t.setName(r.name());
        t.setPrice(r.price());
        t.setQuota(r.quota());
        if (r.sold() != null) t.setSold(r.sold());
    }
}
