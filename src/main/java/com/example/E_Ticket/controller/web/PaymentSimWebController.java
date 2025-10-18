package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.service.CheckoutService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

//test thanh toan, khong quan trong
@Controller
@RequiredArgsConstructor
public class PaymentSimWebController {
    private final CheckoutService checkout;
    private final HttpSession httpSession;

    @GetMapping("/payment/sim")
    public String simPage(@RequestParam String orderCode, Model model){
        model.addAttribute("orderCode", orderCode);
        return "payment/sim";
    }

    @PostMapping("/payment/sim/success")
    public String simSuccess(@RequestParam String orderCode){
        String txnRef = "TXN" + RandomStringUtils.randomNumeric(10);
        // Quan trọng: truyền session id hiện tại để chốt ghế đang giữ
        checkout.markPaid(orderCode, txnRef, httpSession.getId());
        return "redirect:/orders/" + orderCode;
    }

    @PostMapping("/payment/sim/fail")
    public String simFail(@RequestParam String orderCode){
        checkout.markFailed(orderCode, "Simulated failure");
        return "redirect:/orders/" + orderCode;
    }
}
