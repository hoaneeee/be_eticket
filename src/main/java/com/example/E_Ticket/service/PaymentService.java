package com.example.E_Ticket.service;

import com.example.E_Ticket.entity.Payment;

public interface PaymentService {
    Payment create(Long orderId, Payment payment, boolean markOrderPaid);
    Payment updateStatus(Long paymentId, String status, String txnRef);
}
