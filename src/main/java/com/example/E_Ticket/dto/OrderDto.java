package com.example.E_Ticket.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderDto(
        Long id,
        String code,
        Long userId,
        Long eventId,
        BigDecimal total,
        String status,         // PENDING|PAID|CANCELLED|CHECKED_IN
        String paymentMethod,  // COD|BANK|MOMO
        Instant createdAt,
        List<OrderItemDto> items,

        String eventName,
        String userEmail
) {
}
