package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.dto.BankTransferWebhookDto;
import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.entity.Payment;
import com.example.E_Ticket.repository.PaymentRepository;
import com.example.E_Ticket.service.BankTransferService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BankTransferWebController {
    private final BankTransferService bankTransferService;
    private final PaymentRepository paymentRepository;


    @GetMapping("/payment/bank-transfer/pay")
    @PreAuthorize("isAuthenticated()")
    public String paymentPage(@RequestParam String orderCode, Model model) {
        log.info("Bank transfer payment page requested for order: {}", orderCode);

        String qrUrl = bankTransferService.initiatePayment(orderCode);
        
        Order order = bankTransferService.getOrderByCode(orderCode);

        long amount = order.getTotal().longValue();
        String addInfo = "DH" + orderCode;
        String qrImageUrl = String.format(
            "https://img.vietqr.io/image/mbbank-0000153686666-compact.jpg?amount=%d&addInfo=%s",
            amount,
            addInfo.replace(" ", "+")
        );
        
        model.addAttribute("order", order);
        model.addAttribute("qrImageUrl", qrImageUrl);
        model.addAttribute("bankName", "MB Bank (MBBank)");
        model.addAttribute("accountNumber", "0000153686666");
        model.addAttribute("accountHolder", "Vũ Đức Đạt");
        model.addAttribute("amount", order.getTotal());
        model.addAttribute("transferContent", "DH" + orderCode);
        
        return "payment/bank_transfer_qr";
    }

    @GetMapping("/payment/bank-transfer/qr")
    @PreAuthorize("isAuthenticated()")
    public String qrPage(@RequestParam String orderCode, Model model) {
        return paymentPage(orderCode, model);
    }


    @PostMapping("/webhooks/bank-transfer")
    public ResponseEntity<?> handleWebhook(@RequestBody BankTransferWebhookDto webhook) {
        log.info("Received bank transfer webhook: {}", webhook);
        
        try {
            boolean success = bankTransferService.handleWebhook(webhook);
            
            if (success) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment processed successfully"
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "Payment validation failed"
                ));
            }
        } catch (Exception e) {
            log.error("Error processing webhook", e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "Internal server error: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/payment/bank-transfer/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> checkPaymentStatus(@RequestParam String orderCode) {
        try {
            Order order = bankTransferService.getOrderByCode(orderCode);
            Payment payment = paymentRepository.findTopByOrderIdOrderByIdDesc(order.getId())
                    .orElse(null);
            
            if (payment != null && "SUCCESS".equals(payment.getStatus())) {
                return ResponseEntity.ok(Map.of(
                    "status", "SUCCESS",
                    "paid", true,
                    "orderStatus", order.getStatus().toString()
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "status", payment != null ? payment.getStatus() : "PENDING",
                    "paid", false,
                    "orderStatus", order.getStatus().toString()
                ));
            }
        } catch (Exception e) {
            log.error("Error checking payment status", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
}
