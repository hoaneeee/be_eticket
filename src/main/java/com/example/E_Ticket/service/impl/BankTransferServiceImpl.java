package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.dto.BankTransferWebhookDto;
import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.entity.Payment;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.repository.OrderRepository;
import com.example.E_Ticket.repository.PaymentRepository;
import com.example.E_Ticket.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class BankTransferServiceImpl implements BankTransferService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final TicketService ticketService;
    private final QrService qrService;
    private final MailService mailService;

    @Value("${app.web.base-url:http://localhost:8080}")
    private String appBaseUrl;

    private static final String BANK_ACCOUNT = "0000153686666";
    private static final String BANK_NAME = "MBBank";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public String initiatePayment(String orderCode) {
        Order order = orderRepository.findByCode(orderCode)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderCode));

        Payment payment = Payment.builder()
                .order(order)
                .provider("BANK")
                .amount(order.getTotal())
                .status("INIT")
                .attempt(0)
                .build();
        paymentRepository.save(payment);

        order.setPaymentMethod("BANK");
        orderRepository.save(order);

        return "/payment/bank-transfer/qr?orderCode=" + orderCode;
    }

    @Override
    public boolean handleWebhook(BankTransferWebhookDto webhook) {
        if (!"in".equalsIgnoreCase(webhook.transferType())) {
            return false;
        }

        if (!BANK_ACCOUNT.equals(webhook.accountNumber())) {
            return false;
        }
        String content = webhook.content();
        if (content == null || content.trim().isEmpty()) {
            return false;
        }
        content = content.replace(" ", "").replace("+", "");
    
        String orderCode = content;
        if (content.toUpperCase().startsWith("DH")) {
            orderCode = content.substring(2);
        }

        Order order = orderRepository.findByCode(orderCode).orElse(null);
        if (order == null) {
            return false;
        }
        BigDecimal expectedAmount = order.getTotal();
        if (webhook.transferAmount().compareTo(expectedAmount) != 0) {
            return false;
        }
        Payment payment = paymentRepository.findTopByOrderIdOrderByIdDesc(order.getId())
                .orElse(null);

        if (payment == null) {
            payment = Payment.builder()
                    .order(order)
                    .provider("BANK")
                    .amount(webhook.transferAmount())
                    .status("SUCCESS")
                    .txnRef(webhook.referenceCode())
                    .paidAt(Instant.now())
                    .attempt(0)
                    .build();
        } else {
            if ("SUCCESS".equals(payment.getStatus())) {
                return true;
            }
            payment.setStatus("SUCCESS");
            payment.setTxnRef(webhook.referenceCode());
            payment.setPaidAt(Instant.now());
        }

        paymentRepository.save(payment);

        order.setStatus(Order.Status.PAID);
        order.setPaymentMethod("BANK");

        if (order.getQrImagePath() == null) {
            String qrData = """
                 {"orderId":"%s","status":"%s","total":%s}
            """.formatted(order.getCode(), order.getStatus(), order.getTotal());
            String path = qrService.createPng(qrData, order.getCode(), 400);
            order.setQrImagePath("/" + path);
        }

        orderRepository.save(order);

        var tickets = ticketService.issueTickets(order);

        try {
            if (order.getUser() != null && order.getUser().getEmail() != null) {
                StringBuilder html = new StringBuilder("""
                  <h3>Vé của bạn (đơn %s)</h3>
                  <p>Cảm ơn bạn đã thanh toán. Mã vé & QR như bên dưới:</p>
                  <ul>
                """.formatted(order.getCode()));

                for (var t : tickets) {
                    html.append("""
                    <li>
                      <b>%s</b><br>
                      <img src="%s" alt="QR" style="height:160px;border:1px solid #eee;border-radius:8px;margin:6px 0">
                    </li>
                  """.formatted(t.getCode(), appBaseUrl + t.getQrImagePath()));
                }
                html.append("</ul>");

                mailService.send(
                    order.getUser().getEmail(),
                    "Vé của bạn – " + order.getCode(),
                    html.toString()
                );
            }
        } catch (Exception e) {
        }

        return true;
    }

    @Override
    public Order getOrderByCode(String orderCode) {
        return orderRepository.findByCode(orderCode)
                .orElseThrow(() -> new NotFoundException("Order not found: " + orderCode));
    }
}
