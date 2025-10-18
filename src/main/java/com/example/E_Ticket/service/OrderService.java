package com.example.E_Ticket.service;

import com.example.E_Ticket.dto.OrderCreateReq;
import com.example.E_Ticket.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrderService {
    Page<Order> list(Pageable pageable);
    Order getByCode(String code);
    Optional<Order> findByCode(String code);
    /** userId maybe null (guest). return Order đã lưu kèm OrderItems. */
    Order placeOrder(OrderCreateReq req, Long userId, boolean markPaid);
    void cancel(String code);
}
