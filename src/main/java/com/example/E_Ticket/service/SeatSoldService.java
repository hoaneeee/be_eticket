package com.example.E_Ticket.service;

import com.example.E_Ticket.entity.Order;

public interface SeatSoldService {
    /*Chuyển tất cả ghế đang hold bởi sessionId thành sold cho đơn hàng */
    void commitSeatsForOrder(Order order, String sessionId);
}
