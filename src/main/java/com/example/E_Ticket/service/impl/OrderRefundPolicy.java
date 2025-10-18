package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.entity.Order;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class OrderRefundPolicy {
    public boolean isRefundable(Order o){
        if (o == null || o.getStatus() == null) return false;
        if (o.getStatus() != Order.Status.PAID) return false;

        Instant now = Instant.now();
        if (o.getEvent() != null && o.getEvent().getEventDate() != null) {
            return o.getEvent().getEventDate().isAfter(now.plus(48, ChronoUnit.HOURS));
        }
        return o.getCreatedAt() != null && o.getCreatedAt().isAfter(now.minus(24, ChronoUnit.HOURS));
    }

    public String note() {
        return "Chỉ hoàn tiền cho đơn đã thanh toán và sự kiện còn cách ≥ 48h (hoặc trong 24h kể từ khi đặt nếu không có ngày sự kiện).";
    }
}
