package com.example.E_Ticket.controller.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@PreAuthorize("isAuthenticated()")
public class RefundUserPageController {

    @GetMapping("/me/refunds")
    public String refundsPage(){
        return "orders/refundUpdateBagde";
    }
}
