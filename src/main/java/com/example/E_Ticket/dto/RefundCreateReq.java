package com.example.E_Ticket.dto;


import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record RefundCreateReq(
        @NotNull Long orderId,
        @NotNull @Min(1) BigDecimal amount,
        String reason
) {}
