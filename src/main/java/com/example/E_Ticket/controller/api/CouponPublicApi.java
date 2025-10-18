package com.example.E_Ticket.controller.api;

import com.example.E_Ticket.dto.CouponDto;
import com.example.E_Ticket.service.CouponService;
import com.example.E_Ticket.service.impl.CartSessionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/public/v1/coupons")
@RequiredArgsConstructor
public class CouponPublicApi {
    private final CouponService couponService;
    private final CartSessionService cartSession;

    @PostMapping("/apply")
    public Map<String,Object> apply(@RequestBody Map<String,String> body, HttpSession session){
        String code = body.get("code");
        if (code == null || code.isBlank()){
            return Map.of("ok", false, "message", "Chưa nhập mã");
        }
        CouponDto c = couponService.validate(code.trim()); // ném lỗi nếu sai
        cartSession.setCoupon(session, c.code());          // lưu vào session
        return Map.of("ok", true, "coupon", c);
    }

    @PostMapping("/clear")
    public Map<String,Object> clear(HttpSession session){
        cartSession.setCoupon(session, null);
        return Map.of("ok", true);
    }
}
