package com.example.E_Ticket.dto;

public record TicketHoldCreateReq(
        Long eventId,
        Long ticketTypeId,
        Integer qty,
        Long userId,
        String sessionId

) {}