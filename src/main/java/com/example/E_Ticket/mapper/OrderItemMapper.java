package com.example.E_Ticket.mapper;

import com.example.E_Ticket.dto.OrderItemDto;
import com.example.E_Ticket.entity.OrderItem;

public class OrderItemMapper {
    public static OrderItemDto toDto(OrderItem e){
        return new OrderItemDto(
                e.getId(),
                e.getTicketType() != null ? e.getTicketType().getId() : null,
                e.getTicketType() != null ? e.getTicketType().getName() : null,
                e.getQty(),
                e.getPrice()
        );
    }
}
