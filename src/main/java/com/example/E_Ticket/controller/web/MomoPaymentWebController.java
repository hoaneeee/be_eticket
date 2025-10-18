/*

package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.service.MomoPaymentService;
import com.example.E_Ticket.service.impl.CartSessionService;
import com.example.E_Ticket.service.momo.MomoClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/payment/momo")
@RequiredArgsConstructor
public class MomoPaymentWebController {
    private final MomoPaymentService momoService;
    private final MomoClient momoClient;
    private final CartSessionService cartSessionService;

    */
/** Ví MoMo *//*

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/pay")
    public String pay(@RequestParam("orderCode") String orderCode) {
        String payUrl = momoService.createAndGetPayUrl(orderCode, "MOMO_WALLET");
        return "redirect:" + payUrl;
    }

    */
/** Thẻ ATM nội địa (NAPAS) *//*

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/atm/pay")
    public String payAtm(@RequestParam("orderCode") String orderCode) {
        String payUrl = momoService.createAndGetPayUrl(orderCode, "MOMO_ATM");
        return "redirect:" + payUrl;
    }

    @GetMapping("/return")
    public String ret(@RequestParam Map<String,String> params, Model model, HttpServletRequest request) {
        boolean ok = momoClient.verifyReturnSignature(params);
        model.addAttribute("okSig", ok);

        if (ok && "0".equals(params.get("resultCode"))) {
            String orderId   = params.get("orderId");
            String transId   = params.get("transId");
            String requestId = params.get("requestId");

            HttpSession session = request.getSession(false);
            String sessionId = (session != null) ? session.getId() : null;

            // commit ghế + phát hành vé
            Order paid = momoService.markPaid(orderId, transId, requestId, sessionId);

            // dọn giỏ CHỈ của event đã thanh toán
            if (session != null) {
                cartSessionService.clearByEvent(session, paid.getEvent().getId());
            }
        }
        return "payment/momo_return";
    }
    */
/** IPN (server-to-server) *//*

    @PostMapping("/ipn")
    @ResponseBody
    public Map<String, Object> ipn(@RequestBody Map<String, Object> body) {
        try {
            if (!momoClient.verifyIpnSignature(body)) {
                return Map.of("resultCode", 99, "message", "invalid signature");
            }
            int resultCode = Integer.parseInt(String.valueOf(body.getOrDefault("resultCode", "999")));
            String orderId = String.valueOf(body.get("orderId"));
            String transId = String.valueOf(body.get("transId"));
            String requestId = String.valueOf(body.get("requestId"));
            String message = String.valueOf(body.getOrDefault("message", ""));

            if (resultCode == 0) {
                momoService.markPaid(orderId, transId, requestId,null);
            } else {
                momoService.markFailed(orderId, resultCode, message, requestId);
            }
            return Map.of("resultCode", 0, "message", "success");
        } catch (Exception e) {
            return Map.of("resultCode", 99, "message", e.getMessage());
        }
    }
}
*/
package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.service.MomoPaymentService;
import com.example.E_Ticket.service.impl.CartSessionService;
import com.example.E_Ticket.service.momo.MomoClient;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/payment/momo")
@RequiredArgsConstructor
public class MomoPaymentWebController {
    private final MomoPaymentService momoService;
    private final MomoClient momoClient;
    private final CartSessionService cartSessionService;

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/pay")
    public String pay(@RequestParam("orderCode") String orderCode) {
        String payUrl = momoService.createAndGetPayUrl(orderCode, "MOMO_WALLET");
        return "redirect:" + payUrl;
    }

    @PreAuthorize("isAuthenticated()")
    @GetMapping("/atm/pay")
    public String payAtm(@RequestParam("orderCode") String orderCode) {
        String payUrl = momoService.createAndGetPayUrl(orderCode, "MOMO_ATM");
        return "redirect:" + payUrl;
    }

    @GetMapping("/return")
    public String ret(@RequestParam Map<String,String> params, Model model, HttpServletRequest request) {
        boolean okSig = momoClient.verifyReturnSignature(params);
        String resultCode = params.get("resultCode");

        // -------------------------------------------------------
        // FIX: chỉ set ok=true khi chữ ký hợp lệ và resultCode=0
        // -------------------------------------------------------
        boolean ok = okSig && "0".equals(resultCode);
        model.addAttribute("okSig", okSig);      // giữ lại biến cũ nếu nơi khác dùng
        model.addAttribute("ok", ok);            // FIX: thêm biến ok cho view
        model.addAttribute("message", params.getOrDefault("message", ""));

        if (ok) {
            String orderId   = params.get("orderId");
            String transId   = params.get("transId");
            String requestId = params.get("requestId");

            HttpSession session = request.getSession(false);
            String sessionId = (session != null) ? session.getId() : null;

            Order paid = momoService.markPaid(orderId, transId, requestId, sessionId);

            if (session != null) {
                cartSessionService.clearByEvent(session, paid.getEvent().getId());
            }
        }
        return "payment/momo_return";
    }

    @PostMapping("/ipn")
    @ResponseBody
    public Map<String, Object> ipn(@RequestBody Map<String, Object> body) {
        try {
            if (!momoClient.verifyIpnSignature(body)) {
                return Map.of("resultCode", 99, "message", "invalid signature");
            }
            int resultCode = Integer.parseInt(String.valueOf(body.getOrDefault("resultCode", "999")));
            String orderId = String.valueOf(body.get("orderId"));
            String transId = String.valueOf(body.get("transId"));
            String requestId = String.valueOf(body.get("requestId"));
            String message = String.valueOf(body.getOrDefault("message", ""));

            if (resultCode == 0) {
                momoService.markPaid(orderId, transId, requestId, null);
            } else {
                momoService.markFailed(orderId, resultCode, message, requestId);
            }
            return Map.of("resultCode", 0, "message", "success");
        } catch (Exception e) {
            return Map.of("resultCode", 99, "message", e.getMessage());
        }
    }
}
