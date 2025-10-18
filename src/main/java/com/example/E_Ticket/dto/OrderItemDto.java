package com.example.E_Ticket.dto;

import java.math.BigDecimal;

public record OrderItemDto(
        Long id,
        Long ticketTypeId,
        String ticketName,
        Integer qty,
        BigDecimal price
) {
}
