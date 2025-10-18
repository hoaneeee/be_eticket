package com.example.E_Ticket.controller.api;

import com.example.E_Ticket.dto.CouponDto;
import com.example.E_Ticket.dto.CouponUpsertReq;
import com.example.E_Ticket.service.CouponService;
import com.example.E_Ticket.service.impl.CartSessionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/v1/coupons")
@RequiredArgsConstructor
public class CouponAdminApi {
    private final CouponService couponService;
    private final CartSessionService cartSession;
    @GetMapping
    public List<CouponDto> list() {
        return couponService.list();
    }

    @PostMapping
    public CouponDto create( @Valid @RequestBody CouponUpsertReq r) {
        return couponService.create(r);
    }

    @PutMapping("/{id}")
    public CouponDto update(@PathVariable Long id,@Valid @RequestBody CouponUpsertReq r) {
        return couponService.update(id, r);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        couponService.delete(id);
    }

    // test/preview
    @PostMapping("/validate")
    public Map<String, Object> validate(@RequestBody Map<String, String> body) {
        CouponDto c = couponService.validate(body.get("code"));
        return Map.of("ok", true, "coupon", c);
    }

}
