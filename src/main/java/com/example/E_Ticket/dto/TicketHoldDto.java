package com.example.E_Ticket.dto;

import java.time.Instant;

public record TicketHoldDto(
        Long id,
        Long eventId,
        Long ticketTypeId,
        Integer qty,
        String status,
        Instant expiresAt
) {}