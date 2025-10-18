package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.dto.CartView;
import com.example.E_Ticket.service.InventoryService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.example.E_Ticket.dto.TicketHoldCreateReq;
import com.example.E_Ticket.dto.TicketHoldDto;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CartSessionService {
    public static final String CART_KEY = "CART";
    private final InventoryService inventoryService;

    public CartView get(HttpSession session){
        CartView c = (CartView) session.getAttribute(CART_KEY);
        if (c == null){
            c = new CartView();
            session.setAttribute(CART_KEY, c);
        }
        return c;
    }

    public CartView peek(HttpSession session){
        return (CartView) session.getAttribute(CART_KEY);
    }

    public void clear(HttpSession session){
        var cart = peek(session);
        if (cart != null) {
            for (var l : cart.lines) {
                if (l.holdId != null) {
                    try { inventoryService.releaseHold(l.holdId); } catch (Exception ignored) {}
                }
            }
        }
        session.removeAttribute(CART_KEY);
        session.removeAttribute("couponCode");
    }
    public void clearByEvent(HttpSession session, Long eventId) {
        var cart = peek(session);
        if (cart == null || cart.lines == null) return;

        List<CartView.Line> toRemove = new ArrayList<>();
        for (var l : cart.lines) {
            if (l.eventId != null && l.eventId.equals(eventId)) {
                if (l.holdId != null) {
                    try { inventoryService.releaseHold(l.holdId); } catch (Exception ignored) {}
                }
                toRemove.add(l);
            }
        }
        cart.lines.removeAll(toRemove);

        // sạch thì dọn luôn key và coupon
        if (cart.lines.isEmpty()) {
            session.removeAttribute(CART_KEY);
            session.removeAttribute("couponCode");
        }
    }
    public void addSeat(HttpSession session, Long eventId, Long ticketTypeId,
                        String displayName, long price, int qty){
        var cart = get(session);

        var holdDto = inventoryService.createHold(
                new TicketHoldCreateReq(eventId, ticketTypeId, qty, null, session.getId())
        );

        CartView.Line line = new CartView.Line();
        line.eventId = eventId;
        line.ticketTypeId = ticketTypeId;
        line.ticketTypeName = displayName;
        line.unitPrice = price;
        line.qty = qty;

        line.holdId = holdDto.id();
        line.holdExpiresAt = holdDto.expiresAt();

        cart.lines.add(line);
    }

    public void removeByTicketType(HttpSession session, Long ticketTypeId){
        var cart = get(session);
        var it = cart.lines.iterator();
        while (it.hasNext()){
            var l = it.next();
            if (l.ticketTypeId != null && l.ticketTypeId.equals(ticketTypeId)) {
                if (l.holdId != null) {
                    try { inventoryService.releaseHold(l.holdId); } catch (Exception ignored) {}
                }
                it.remove();
                break;
            }
        }
    }
    public void removeSeatByName(HttpSession session, String displayName){
        var cart = get(session);
        var it = cart.lines.iterator();
        while (it.hasNext()){
            var l = it.next();
            if (displayName.equals(l.ticketTypeName)) {
                if (l.holdId != null) {
                    try { inventoryService.releaseHold(l.holdId); } catch (Exception ignored) {}
                }
                it.remove();
                break;
            }
        }
    }
    public void setCoupon(HttpSession session, String code) {
        session.setAttribute("couponCode", code);
    }

    public String getCoupon(HttpSession session) {
        Object v = session.getAttribute("couponCode");
        return v != null ? v.toString() : null;
    }

    public int purgeExpired(HttpSession session){
        var cart = peek(session);
        if (cart == null || cart.lines == null) return 0;

        int removed;
        Instant now = Instant.now();

        synchronized (cart) { // <- chặn song song
            List<CartView.Line> toRemove = new ArrayList<>();

            for (var l : cart.lines) {
                boolean expiredByTime = (l.holdExpiresAt != null && l.holdExpiresAt.isBefore(now));
                boolean inactiveOnSrv = false;
                if (l.holdId != null) {
                    try { inactiveOnSrv = !inventoryService.isHoldActive(l.holdId); }
                    catch (Exception ignored) {}
                }

                if (expiredByTime || inactiveOnSrv) {
                    if (l.holdId != null) {
                        try { inventoryService.releaseHold(l.holdId); } catch (Exception ignored) {}
                    }
                    toRemove.add(l);
                }
            }
            cart.lines.removeAll(toRemove);
            removed = toRemove.size();
        }
        return removed;
    }
    public void consumeAllActiveHolds(HttpSession session) {
        var cart = peek(session);
        if (cart == null || cart.lines == null) return;

        for (var l : cart.lines) {
            if (l.holdId != null) {
                try { inventoryService.consumeHold(l.holdId); } catch (Exception ignored) {}
            }
        }
    }
}
