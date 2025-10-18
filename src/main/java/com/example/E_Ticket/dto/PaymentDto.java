package com.example.E_Ticket.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentDto(
        Long id,
        Long orderId,
        String provider, // MOMO|BANK|COD
        BigDecimal amount,
        String status,   // INIT|SUCCESS|FAILED|REFUNDED
        String txnRef,
        Instant paidAt,
        Instant createdAt
) {
}
