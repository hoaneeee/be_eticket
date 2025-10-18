/*
// src/main/java/com/example/E_Ticket/controller/web/RefundWebController.java
package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.dto.RefundCreateReq;
import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.repository.OrderRepository;
import com.example.E_Ticket.repository.RefundRepository;
import com.example.E_Ticket.repository.UserRepository;
import com.example.E_Ticket.service.RefundService;
import com.example.E_Ticket.service.impl.OrderRefundPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class RefundWebController {
    private final OrderRepository orderRepo;
    private final RefundRepository refundRepo;
    private final UserRepository userRepo;
    private final RefundService refundService;
    private final OrderRefundPolicy orderRefundPolicy;

    */
/* GET /orders/{code}/refund – hien thi  form *//*

    @GetMapping("/{code}/refund")
    public String refundForm(@PathVariable String code,
                             Authentication auth, Model model){
        var user = userRepo.findByEmail(auth.getName()).orElseThrow();
        Order o = orderRepo.findByCodeAndUser_Id(code, user.getId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        String error = null;
        if (!orderRefundPolicy.isRefundable(o)) {
            error = "Đơn hàng không đủ điều kiện hoàn tiền.";
        } else if (!refundRepo.findByOrder_Id(o.getId()).isEmpty()) {
            error = "Bạn đã gửi yêu cầu hoàn tiền cho đơn này.";
        }

        model.addAttribute("order", o);
        model.addAttribute("code", o.getCode());
        model.addAttribute("maxAmount", o.getTotal());  // BigDecimal
        model.addAttribute("note", orderRefundPolicy.note());
        model.addAttribute("error", error);
        return "orders/refund_form";
    }

    */
/* POST /orders/{code}/refund – create Refund(PENDING) *//*

    @PostMapping("/{code}/refund")
    public String refundSubmit(@PathVariable String code,
                               @RequestParam("amount") BigDecimal amount,
                               @RequestParam("reason") String reason,
                               Authentication auth){
        var user = userRepo.findByEmail(auth.getName()).orElseThrow();
        Order o = orderRepo.findByCodeAndUser_Id(code, user.getId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!orderRefundPolicy.isRefundable(o)) {
            return "redirect:/orders/%s?err=not_refundable".formatted(code);
        }
        if (!refundRepo.findByOrder_Id(o.getId()).isEmpty()) {
            return "redirect:/orders/%s?err=duplicate".formatted(code);
        }
        if (amount == null || amount.signum() <= 0 || amount.compareTo(o.getTotal()) > 0) {
            return "redirect:/orders/%s?err=amount".formatted(code);
        }

        // Tận dụng RefundService.create (đã kiểm tra PAID & <= total)
        refundService.create(new RefundCreateReq(o.getId(), amount, reason), user.getEmail());

        // quay về trang chi tiết đơn, có param báo gửi thành công
        return "redirect:/orders/%s?refund_submitted".formatted(code);
    }
}
*/
package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.dto.RefundCreateReq;
import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.repository.OrderRepository;
import com.example.E_Ticket.repository.RefundRepository;
import com.example.E_Ticket.repository.UserRepository;
import com.example.E_Ticket.service.RefundService;
import com.example.E_Ticket.service.impl.OrderRefundPolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")
public class RefundWebController {
    private final OrderRepository orderRepo;
    private final RefundRepository refundRepo;
    private final UserRepository userRepo;
    private final RefundService refundService;
    private final OrderRefundPolicy policy;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/{code}/refund")
    public String refundForm(@PathVariable String code, Authentication auth, Model model){
        var user = userRepo.findByEmail(auth.getName()).orElseThrow();
        Order o = orderRepo.findByCodeAndUser_Id(code, user.getId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        String error = null;
        if (!policy.isRefundable(o)) {
            error = "Đơn hàng không đủ điều kiện hoàn tiền.";
        } else if (!refundRepo.findByOrder_Id(o.getId()).isEmpty()) {
            error = "Bạn đã gửi yêu cầu hoàn tiền cho đơn này.";
        }

        model.addAttribute("order", o);
        model.addAttribute("code", o.getCode());
        model.addAttribute("maxAmount", o.getTotal());
        model.addAttribute("note", policy.note());
        model.addAttribute("error", error);
        return "orders/refund_form";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/{code}/refund")
    public String refundSubmit(@PathVariable String code,
                               @RequestParam("amount") BigDecimal amount,
                               @RequestParam("reason") String reason,
                               Authentication auth){
        var user = userRepo.findByEmail(auth.getName()).orElseThrow();
        Order o = orderRepo.findByCodeAndUser_Id(code, user.getId())
                .orElseThrow(() -> new NotFoundException("Order not found"));

        if (!policy.isRefundable(o))    return "redirect:/orders/%s?err=not_refundable".formatted(code);
        if (!refundRepo.findByOrder_Id(o.getId()).isEmpty()) return "redirect:/orders/%s?err=duplicate".formatted(code);
        if (amount == null || amount.signum() <= 0 || amount.compareTo(o.getTotal()) > 0)
            return "redirect:/orders/%s?err=amount".formatted(code);

        refundService.create(new RefundCreateReq(o.getId(), amount, reason), user.getEmail());
        return "redirect:/orders/%s?refund_submitted".formatted(code);
    }
}
