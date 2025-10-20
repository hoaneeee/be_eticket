/*
package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.config.WebUtils;
import com.example.E_Ticket.dto.CartView;
import com.example.E_Ticket.dto.PricingPreviewReq;
import com.example.E_Ticket.service.CheckoutService;
import com.example.E_Ticket.service.InventoryService;
import com.example.E_Ticket.service.PricingService;
import com.example.E_Ticket.service.impl.CartSessionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.print.PrintService;

@Controller
@RequiredArgsConstructor
public class CheckoutWebController
{
   */
/* private final CartSessionService cartSessionService;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;
    private final WebUtils webUtils;
    private final InventoryService inventoryService;
    @GetMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public String checkoutPage(HttpSession session, Model model) {
        CartView cart = cartSessionService.get(session);
        if (cart == null || cart.lines == null || cart.lines.isEmpty()) {
            return "redirect:/cart";
        }

        //  hold phải còn active
        boolean allActive = cart.lines.stream()
                .filter(l -> l.holdId != null)
                .allMatch(l -> inventoryService.isHoldActive(l.holdId));
        if (!allActive) {
            model.addAttribute("error","Một số vé đã hết hạn giữ chỗ. Vui lòng gia hạn hoặc chọn lại.");
            model.addAttribute("cart", cart);
            return "cart/index";
        }

        String couponCode = cartSessionService.getCoupon(session);
        model.addAttribute("cart", cart);
        model.addAttribute("couponCode", couponCode);
        var preview = pricingService.preview(session, new PricingPreviewReq(couponCode));
        model.addAttribute("preview", preview);

        return "checkout/index";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/checkout/place-order")
    public String placeOrder(@RequestParam String buyerName,
                             @RequestParam String buyerEmail,
                             @RequestParam(required = false) String buyerPhone,
                             HttpSession session){
        CartView cart = cartSessionService.get(session);

        // Re-check holds trước khi chốt
        for (var l : cart.lines){
            if (l.holdId != null && !inventoryService.isHoldActive(l.holdId)){
                return "redirect:/cart";
            }
        }

        Long userId = webUtils.currentUserIdOrNull();
        var order = checkoutService.placeOrder(cart, userId, buyerName, buyerEmail, buyerPhone);

        // Consume tất cả hold sau khi tạo order
        for (var l : cart.lines){
            if (l.holdId != null) {
                try { inventoryService.consumeHold(l.holdId); } catch (Exception ignored) {}
            }
        }

        cart.lines.clear();
        return "redirect:/payment/momo/pay?orderCode=" + order.getCode();
    }*//*

   private final CartSessionService cartSessionService;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;
    private final WebUtils webUtils;
    private final InventoryService inventoryService;

    @GetMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public String checkoutPage(HttpSession session, Model model) {
        CartView cart = cartSessionService.get(session);
        if (cart == null || cart.lines == null || cart.lines.isEmpty()) {
            return "redirect:/cart";
        }

        // hold phải còn active (chỉ kiểm tra những line có holdId)
        boolean allActive = cart.lines.stream()
                .filter(l -> l.holdId != null)
                .allMatch(l -> inventoryService.isHoldActive(l.holdId));
        if (!allActive) {
            model.addAttribute("error","Một số vé đã hết hạn giữ chỗ. Vui lòng gia hạn hoặc chọn lại.");
            model.addAttribute("cart", cart);
            return "cart/index";
        }

        String couponCode = cartSessionService.getCoupon(session);
        model.addAttribute("cart", cart);
        model.addAttribute("couponCode", couponCode);
        var preview = pricingService.preview(session, new PricingPreviewReq(couponCode));
        model.addAttribute("preview", preview);

        return "checkout/index";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/checkout/place-order")
    public String placeOrder(@RequestParam String buyerName,
                             @RequestParam String buyerEmail,
                             @RequestParam(required = false) String buyerPhone,
                             @RequestParam(defaultValue = "MOMO_WALLET") String paymentMethod,
                             HttpSession session,
                             Model model) {
        CartView cart = cartSessionService.get(session);
        if (cart == null || cart.lines == null || cart.lines.isEmpty()) return "redirect:/cart";

        boolean allActive = cart.lines.stream()
                .filter(l -> l.holdId != null)
                .allMatch(l -> inventoryService.isHoldActive(l.holdId));
        if (!allActive) {
            model.addAttribute("error","Một số vé đã hết hạn giữ chỗ. Vui lòng gia hạn hoặc chọn lại.");
            model.addAttribute("cart", cart);
            return "cart/index";
        }

        Long userId = webUtils.currentUserIdOrNull();
        var order = checkoutService.placeOrder(cart, userId, buyerName, buyerEmail, buyerPhone);

        return switch (paymentMethod) {
            case "MOMO_ATM"    -> "redirect:/payment/momo/atm/pay?orderCode=" + order.getCode();
            case "COD"         -> "redirect:/orders/" + order.getCode() + "?pending_cod=1";
            default            -> "redirect:/payment/momo/pay?orderCode=" + order.getCode();
        };
    }

}
*/
package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.config.WebUtils;
import com.example.E_Ticket.dto.CartView;
import com.example.E_Ticket.dto.PricingPreviewReq;
import com.example.E_Ticket.service.CheckoutService;
import com.example.E_Ticket.service.InventoryService;
import com.example.E_Ticket.service.PricingService;
import com.example.E_Ticket.service.impl.CartSessionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class CheckoutWebController {
    private final CartSessionService cartSessionService;
    private final CheckoutService checkoutService;
    private final PricingService pricingService;
    private final WebUtils webUtils;
    private final InventoryService inventoryService;

    @GetMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public String checkoutPage(HttpSession session, Model model) {
        CartView cart = cartSessionService.get(session);
        if (cart == null || cart.lines == null || cart.lines.isEmpty()) {
            return "redirect:/cart";
        }

        // hold phải còn active (chỉ kiểm tra những line có holdId)
        boolean allActive = cart.lines.stream()
                .filter(l -> l.holdId != null)
                .allMatch(l -> inventoryService.isHoldActive(l.holdId));
        if (!allActive) {
            model.addAttribute("error", "Một số vé đã hết hạn giữ chỗ. Vui lòng gia hạn hoặc chọn lại.");
            model.addAttribute("cart", cart);
            return "cart/index";
        }

        String couponCode = cartSessionService.getCoupon(session);
        model.addAttribute("cart", cart);
        model.addAttribute("couponCode", couponCode);
        var preview = pricingService.preview(session, new PricingPreviewReq(couponCode));
        model.addAttribute("preview", preview);
        return "checkout/index";
    }

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/checkout/place-order")
    public String placeOrder(@RequestParam String buyerName,
                             @RequestParam String buyerEmail,
                             @RequestParam(required = false) String buyerPhone,
                             @RequestParam(defaultValue = "MOMO_WALLET") String paymentMethod,
                             HttpSession session,
                             Model model) {
        CartView cart = cartSessionService.get(session);
        if (cart == null || cart.lines == null || cart.lines.isEmpty()) return "redirect:/cart";

        boolean allActive = cart.lines.stream()
                .filter(l -> l.holdId != null)
                .allMatch(l -> inventoryService.isHoldActive(l.holdId));
        if (!allActive) {
            model.addAttribute("error", "Một số vé đã hết hạn giữ chỗ. Vui lòng gia hạn hoặc chọn lại.");
            model.addAttribute("cart", cart);
            return "cart/index";
        }

        Long userId = webUtils.currentUserIdOrNull();

        // lấy coupon hiện có + preview để tính tổng sau giảm
        String couponCode = cartSessionService.getCoupon(session);
        var preview = pricingService.preview(session, new PricingPreviewReq(couponCode));

        var payable = preview.total();

        var order = checkoutService.placeOrder(cart, userId, buyerName, buyerEmail, buyerPhone,
                payable);

        return switch (paymentMethod) {
            case "MOMO_ATM" -> "redirect:/payment/momo/atm/pay?orderCode=" + order.getCode();
            case "COD" -> "redirect:/orders/" + order.getCode() + "?pending_cod=1";
            case "BANK_TRANSFER" -> "redirect:/payment/bank-transfer/pay?orderCode=" + order.getCode();
            default -> "redirect:/payment/momo/pay?orderCode=" + order.getCode();
        };
    }
}
