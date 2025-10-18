package com.example.E_Ticket.mapper;

import com.example.E_Ticket.dto.OrderDto;
import com.example.E_Ticket.dto.OrderItemDto;
import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.entity.OrderItem;

import java.util.List;

public class OrderMapper {
    public static OrderDto toDto(Order o) {
        List<OrderItemDto> items = o.getItems() == null ? List.of() :
                o.getItems().stream().map(OrderMapper::toDto).toList();
        return new OrderDto(
                o.getId(), o.getCode(),
                o.getUser() != null ? o.getUser().getId() : null,
                o.getEvent() != null ? o.getEvent().getId() : null,
                o.getTotal(),
                o.getStatus().name(),
                o.getPaymentMethod(),
                o.getCreatedAt(),
                items,   o.getEvent() != null ? o.getEvent().getTitle() : null,
                o.getUser()  != null ? o.getUser().getEmail() : null
        );
    }

    public static OrderItemDto toDto(OrderItem i) {
        return new OrderItemDto(
                i.getId(),
                i.getTicketType() != null ? i.getTicketType().getId() : null,
                i.getTicketType() != null ? i.getTicketType().getName() : null,
                i.getQty(),
                i.getPrice()
        );
    }
}
