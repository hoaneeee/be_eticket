package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.dto.PricingPreviewReq;
import com.example.E_Ticket.dto.PricingPreviewRes;
import com.example.E_Ticket.entity.Coupon;
import com.example.E_Ticket.entity.PriceRule;
import com.example.E_Ticket.entity.TicketType;
import com.example.E_Ticket.repository.CouponRepository;
import com.example.E_Ticket.repository.PriceRuleRepository;
import com.example.E_Ticket.repository.TicketTypeRepository;
import com.example.E_Ticket.repository.ZonePriceRepository;
import com.example.E_Ticket.service.PricingService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final ZonePriceRepository zonePriceRepo;
    private final TicketTypeRepository ticketRepo;
    private final CartSessionService cartSession;
    private final CouponRepository couponRepo;
    private final PriceRuleRepository ruleRepo;

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getFinalPrice(Long eventId, Long ticketTypeId, Long seatZoneId) {
        if (seatZoneId == null) {
            return ticketRepo.findById(ticketTypeId)
                    .map(TicketType::getPrice)
                    .orElse(BigDecimal.ZERO);
        }

        return zonePriceRepo.findByEvent_IdAndTicketType_IdAndSeatZone_Id(eventId, ticketTypeId, seatZoneId)
                .map(zp -> zp.getPrice())
                .orElse(
                        ticketRepo.findById(ticketTypeId)
                                .map(TicketType::getPrice)
                                .orElse(BigDecimal.ZERO)
                );
    }

    /* ==================== 2) Preview (Cart) cho phia User ==================== */
    private static BigDecimal bd(long v){ return BigDecimal.valueOf(v); }
    private static boolean hasPos(BigDecimal x){ return x != null && x.signum() > 0; }

    @Override
    @Transactional(readOnly = true)
    public PricingPreviewRes preview(HttpSession session, PricingPreviewReq req) {
        var cart = cartSession.get(session);
// dang dung long chuyen qua bigdecimal cho dung
        BigDecimal subtotal = cart.lines.stream()
                .map(l -> bd(l.unitPrice).multiply(BigDecimal.valueOf(l.qty)))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2.2 Price Rules theo event trong giỏ
        BigDecimal rulesDiscount = BigDecimal.ZERO;
        List<PricingPreviewRes.RuleBadge> badges = new ArrayList<>();
        Instant now = Instant.now();

        var eventIds = cart.lines.stream().map(l -> l.eventId).distinct().toList();
        for (Long eventId : eventIds){
            var actives = ruleRepo.findActiveByEventAt(eventId, now);

            BigDecimal eventSub = cart.lines.stream()
                    .filter(l -> eventId.equals(l.eventId))
                    .map(l -> bd(l.unitPrice).multiply(BigDecimal.valueOf(l.qty)))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            for (PriceRule r : actives){
                BigDecimal d = BigDecimal.ZERO;
                if (hasPos(r.getPercentOff())){
                    d = eventSub.multiply(r.getPercentOff())
                            .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);
                } else if (hasPos(r.getAmountOff())){
                    d = r.getAmountOff().min(eventSub);
                }
                rulesDiscount = rulesDiscount.add(d);

                String msg = hasPos(r.getPercentOff())
                        ? "Giảm " + r.getPercentOff().stripTrailingZeros().toPlainString() + "% đến " + r.getEndsAt()
                        : (hasPos(r.getAmountOff())
                        ? "Giảm " + r.getAmountOff() + "đ đến " + r.getEndsAt()
                        : "Đang ưu đãi");

                badges.add(new PricingPreviewRes.RuleBadge(
                        r.getLabel()!=null ? r.getLabel() : r.getKind().name(),
                        r.getKind().name(),
                        r.getEndsAt(),
                        msg
                ));
            }
        }

        // 2.3 Coupon (priority code from request; if empty get code and save session)
        String code = (req!=null && req.couponCode()!=null && !req.couponCode().isBlank())
                ? req.couponCode().trim()
                : cartSession.getCoupon(session);

        boolean couponValid = false;
        String couponMessage = null;
        BigDecimal couponDiscount = BigDecimal.ZERO;

        if (code != null && !code.isBlank()){
            var opt = couponRepo.findByCodeIgnoreCase(code);
            if (opt.isEmpty()){
                couponMessage = "Mã không tồn tại.";
            } else {
                var c = opt.get();
                if (c.getStartAt()!=null && now.isBefore(c.getStartAt())){
                    couponMessage = "Mã chưa bắt đầu.";
                } else if (c.getEndAt()!=null && now.isAfter(c.getEndAt())){
                    couponMessage = "Mã đã hết hạn.";
                } else if (c.getMaxUse()!=null && c.getUsed()!=null && c.getUsed()>=c.getMaxUse()){
                    couponMessage = "Mã đã hết lượt.";
                } else {
                    couponValid = true;
                    if (c.getType() == Coupon.Type.PERCENT){
                        couponDiscount = subtotal
                                .multiply(BigDecimal.valueOf(c.getValue()))
                                .divide(BigDecimal.valueOf(100), 0, RoundingMode.FLOOR);
                    } else { // AMOUNT
                        couponDiscount = BigDecimal.valueOf(c.getValue()).min(subtotal);
                    }
                }
            }
        }

        // 2.4 Total
        BigDecimal total = subtotal.subtract(rulesDiscount).subtract(couponDiscount);
        if (total.signum() < 0) total = BigDecimal.ZERO;

        return new PricingPreviewRes(
                subtotal,                // subtotal
                rulesDiscount,           // discountRules
                couponDiscount,          // discountCoupon
                total,                   // total
                couponValid,             // couponValid
                couponMessage,           // couponMessage
                badges                   // ruleBadges
        );
    }
}
