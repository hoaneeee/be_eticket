package com.example.E_Ticket.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record PaymentUpsertReq(
        @NotNull Long orderId,
        @NotBlank String provider,
        @NotNull @DecimalMin("0.0") BigDecimal amount,
        @NotBlank String status,
        String txnRef
) {
}
