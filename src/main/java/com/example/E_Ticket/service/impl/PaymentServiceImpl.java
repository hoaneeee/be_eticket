package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.entity.Payment;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.repository.OrderRepository;
import com.example.E_Ticket.repository.PaymentRepository;
import com.example.E_Ticket.service.PaymentService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;

@Service
@Transactional
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    @Override
    public Payment create(Long orderId, Payment payment, boolean markOrderPaid) {
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new NotFoundException("Order not found"));
        payment.setOrder(order);
        Payment newPayment = paymentRepository.save(payment);
        if (markOrderPaid && "SUCESS".equalsIgnoreCase(newPayment.getStatus())) {
            order.setStatus(Order.Status.PENDING);
        }
        return newPayment;
    }

    @Override
    public Payment updateStatus(Long paymentId, String status, String txnRef) {
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new NotFoundException("Payment not found"));
        payment.setStatus(status);
        if (txnRef != null) payment.setTxnRef(txnRef);
        if ("SUCESS".equalsIgnoreCase(status)) {
            payment.getOrder().setStatus(Order.Status.PENDING);
        }else if ("FAILED".equalsIgnoreCase(status)) {
            payment.getOrder().setStatus(Order.Status.CANCELLED);
        }
        return payment;
    }
}
