package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.dto.RefundDto;
import com.example.E_Ticket.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/web/me/refunds")
@RequiredArgsConstructor
public class RefundUserApi {

    private final RefundService refundService;

    @GetMapping("/updates-count")
    public Map<String, Long> updatesCount(Authentication auth) {
        String email = auth.getName();
        long c = refundService.countRecentUpdatesOfUserEmail(email);
        return Map.of("count", c);     // return JSON {"count": ...}
    }
    @GetMapping("/recent-updates")
    public List<RefundDto> recentUpdates(Authentication auth) {
        String email = auth.getName();
        return refundService.recentUpdatesOfUserEmail(email);
    }
}
