package com.example.E_Ticket.dto;

import java.time.Instant;

public record CheckinDto(
        Long id,
        String code,
        Long orderId,
        Long scannedBy,
        Instant scannedAt
) {
}
