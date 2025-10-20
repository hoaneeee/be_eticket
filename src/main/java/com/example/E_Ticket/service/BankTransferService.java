package com.example.E_Ticket.service;

import com.example.E_Ticket.dto.BankTransferWebhookDto;
import com.example.E_Ticket.entity.Order;

public interface BankTransferService {

    String initiatePayment(String orderCode,String sessionId );
    boolean handleWebhook(BankTransferWebhookDto webhook);
    Order getOrderByCode(String orderCode);
}
