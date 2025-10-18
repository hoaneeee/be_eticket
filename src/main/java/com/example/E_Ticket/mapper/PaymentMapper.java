package com.example.E_Ticket.mapper;

import com.example.E_Ticket.dto.PaymentDto;
import com.example.E_Ticket.dto.PaymentUpsertReq;
import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.entity.Payment;

public class PaymentMapper {
    public static PaymentDto toDto(Payment p){
        return new PaymentDto(
                p.getId(),
                p.getOrder() != null ? p.getOrder().getId() : null,
                p.getProvider(), p.getAmount(), p.getStatus(),
                p.getTxnRef(), p.getPaidAt(), p.getCreatedAt()
        );
    }
    public static Payment toEntity(PaymentUpsertReq r, Order order){
        Payment p = new Payment();
        p.setOrder(order);
        p.setProvider(r.provider());
        p.setAmount(r.amount());
        p.setStatus(r.status());
        p.setTxnRef(r.txnRef());
        return p;
    }
}
