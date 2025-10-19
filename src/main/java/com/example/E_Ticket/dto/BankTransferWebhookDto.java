package com.example.E_Ticket.dto;

import java.math.BigDecimal;

public record BankTransferWebhookDto(
        String gateway,
        String transactionDate,
        String accountNumber,
        String code,
        String content,
        String transferType,
        BigDecimal transferAmount,
        BigDecimal accumulated,
        String referenceCode,
        String description
) {
}
