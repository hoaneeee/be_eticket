package com.example.E_Ticket.controller.api;

import com.example.E_Ticket.dto.PricingPreviewReq;
import com.example.E_Ticket.dto.PricingPreviewRes;
import com.example.E_Ticket.service.PricingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/v1/pricing")
@RequiredArgsConstructor
public class PricingPublicApi {
    private final PricingService pricing;

    @PostMapping(
            path = "/preview",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public PricingPreviewRes preview(
            @RequestBody(required = false) PricingPreviewReq req,
            HttpSession session) {
        return pricing.preview(session, req);
    }
}