package com.example.E_Ticket.dto;

import java.math.BigDecimal;

public record TicketTypeDto(
        Long id,
        Long eventId,
        String name,
        BigDecimal price,
        Integer quota,
        Integer sold
) { }
