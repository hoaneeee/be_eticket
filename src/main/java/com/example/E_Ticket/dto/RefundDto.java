package com.example.E_Ticket.dto;

import java.math.BigDecimal;

public record RefundDto(
        Long id, Long orderId, BigDecimal amount, String reason,
        String status, String note, String createdBy, java.time.Instant createdAt
) {}