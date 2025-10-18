package com.example.E_Ticket.service;

import com.example.E_Ticket.dto.PricingPreviewReq;
import com.example.E_Ticket.dto.PricingPreviewRes;
import jakarta.servlet.http.HttpSession;

import java.math.BigDecimal;

public interface PricingService {
    BigDecimal getFinalPrice(Long eventId, Long ticketTypeId, Long seatZoneId);
    PricingPreviewRes preview(HttpSession session, PricingPreviewReq req);
}
