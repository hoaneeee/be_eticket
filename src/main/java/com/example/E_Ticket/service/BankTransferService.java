package com.example.E_Ticket.service;

import com.example.E_Ticket.dto.BankTransferWebhookDto;
import com.example.E_Ticket.entity.Order;

/**
 * Service xử lý thanh toán chuyển khoản ngân hàng
 */
public interface BankTransferService {

    String initiatePayment(String orderCode);
    boolean handleWebhook(BankTransferWebhookDto webhook);
    Order getOrderByCode(String orderCode);
}
