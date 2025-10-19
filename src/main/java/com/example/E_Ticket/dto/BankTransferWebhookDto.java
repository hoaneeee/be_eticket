package com.example.E_Ticket.dto;

import java.math.BigDecimal;

/**
 * DTO để nhận webhook từ hệ thống ngân hàng
 */
public record BankTransferWebhookDto(
        String gateway,
        String transactionDate,
        String accountNumber,
        String content,
        String transferType,
        BigDecimal transferAmount,
        BigDecimal accumulated,
        String referenceCode,
        String description
) {
}
