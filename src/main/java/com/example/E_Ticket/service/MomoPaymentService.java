package com.example.E_Ticket.service;

import com.example.E_Ticket.entity.Order;

public interface MomoPaymentService {
    String createAndGetPayUrl(String orderCode, String method,String sessionId);

    // đổi: trả về Order + nhận sessionId để commit ghế
    Order markPaid(String momoOrderId, String transId, String requestId, String sessionId);

    void markFailed(String momoOrderId, Integer resultCode, String message, String requestId);
}
