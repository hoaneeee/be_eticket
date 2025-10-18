package com.example.E_Ticket.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PricingPreviewRes(
        BigDecimal subtotal,
        BigDecimal discountRules,
        BigDecimal discountCoupon,
        BigDecimal total,
        boolean couponValid,
        String couponMessage,
        List<RuleBadge> ruleBadges
){
    public record RuleBadge(String label, String kind, Instant endsAt, String message) {}
}
