package com.example.E_Ticket.service;

import com.example.E_Ticket.dto.CartView;
import com.example.E_Ticket.entity.Order;

public interface CheckoutService {
    /* order + payment o trang thai khoi tao (PENDING/INIT) */
    Order placeOrder(CartView cart, Long userId, String buyerName, String buyerEmail, String buyerPhone);
    /* xu ly ket qua thanh toan gia lap */
    void markPaid(String orderCode, String txnId);
    void markPaid(String orderCode, String txnId, String sessionId);
    void markFailed(String orderCode, String reason);
}