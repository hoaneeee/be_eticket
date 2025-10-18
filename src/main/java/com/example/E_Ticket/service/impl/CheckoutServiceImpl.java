package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.dto.CartView;
import com.example.E_Ticket.entity.*;
import com.example.E_Ticket.exception.BusinessException;
import com.example.E_Ticket.repository.*;
import com.example.E_Ticket.service.CheckoutService;
import com.example.E_Ticket.service.SeatSoldService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Transactional
public class CheckoutServiceImpl implements CheckoutService {

    private final OrderRepository orderRepo;
    private final OrderItemRepository orderItemRepo;
    private final PaymentRepository paymentRepo;
    private final TicketTypeRepository ticketTypeRepo;
    private final UserRepository userRepo;
    private final SeatSoldService seatSoldService;

    @Override
    public Order placeOrder(CartView cart, Long userId, String buyerName, String buyerEmail, String buyerPhone) {
        if (cart == null || cart.lines == null || cart.lines.isEmpty()) {
            throw new BusinessException("Cart is empty");
        }
        BigDecimal total  = BigDecimal.ZERO;
        Event orderEvent = null;

        for (var line : cart.lines) {
            var tt = ticketTypeRepo.findById(line.ticketTypeId)
                    .orElseThrow( () -> new BusinessException("Ticket type not found"));

            if (orderEvent == null) orderEvent = tt.getEvent();
            else if (!orderEvent.getId().equals(tt.getEvent().getId())) {
                throw new BusinessException("Giỏ hàng chứa vé nhiều sự kiện");
            }
            BigDecimal unit = unitPriceFromCartOrTicket(line,tt);
            total = total.add(unit.multiply(BigDecimal.valueOf(line.qty)));
        }

        Order order = new Order();
        order.setCode(genOrderCode());
        if (userId != null){
            order.setUser(userRepo.findById(userId).orElseThrow( () -> new BusinessException("User not found")));
        }
        order.setEvent(orderEvent);
        order.setTotal(total);
        order.setStatus(Order.Status.PENDING);
        order.setPaymentMethod("BANK");
        order.setCreatedAt(Instant.now());
        order = orderRepo.save(order);

        for (var l : cart.lines) {
            var tt = ticketTypeRepo.findById(l.ticketTypeId).orElseThrow();
            OrderItem it = new OrderItem();
            it.setOrder(order);
            it.setTicketType(tt);
            it.setQty(l.qty);
            it.setPrice(unitPriceFromCartOrTicket(l, tt));
            orderItemRepo.save(it);
        }

        Payment pm = new Payment();
        pm.setOrder(order);
        pm.setProvider("BANK");
        pm.setAmount(total);
        pm.setStatus("INIT");
        pm.setCreatedAt(Instant.now());
        paymentRepo.save(pm);

        return order;
    }

    @Override
    public void markPaid(String orderCode, String txnId) {
        markPaid(orderCode, txnId, null);
    }

    @Override
    public void markPaid(String code, String txn, String sessionId){
        Order od = orderRepo.findByCode(code)
                .orElseThrow(() -> new BusinessException("Order không tồn tại"));
        Payment pm = paymentRepo.findByOrderId(od.getId())
                .orElseThrow(() -> new BusinessException("Payment không tồn tại"));

        pm.setStatus("SUCCESS"); pm.setTxnRef(txn); pm.setPaidAt(Instant.now());
        paymentRepo.save(pm);

        od.setStatus(Order.Status.PAID);
        orderRepo.save(od);

        // 1) chot ghe dang giu -> sold_seat ( SVG hien thi do)
        if (sessionId != null && od.getEvent() != null) {
            seatSoldService.commitSeatsForOrder(od, sessionId);
        }

        // 2)tru ton kho theo ticket type – lay tu repository tranh lazy
        var items = orderItemRepo.findByOrderId(od.getId());
        for (var it : items){
            var tt = it.getTicketType();
            int sold = tt.getSold() == null ? 0 : tt.getSold();
            tt.setSold(sold + it.getQty());
            ticketTypeRepo.save(tt);
        }
    }

    @Override
    public void markFailed(String orderCode, String reason) {
        Order od = orderRepo.findByCode(orderCode)
                .orElseThrow(() -> new BusinessException("Order không tồn tại"));
        Payment pm = paymentRepo.findByOrderId(od.getId())
                .orElseThrow(() -> new BusinessException("Payment không tồn tại"));

        pm.setStatus("FAILED");
        pm.setTxnRef(reason);
        pm.setPaidAt(Instant.now());
        paymentRepo.save(pm);

        od.setStatus(Order.Status.CANCELLED);
        orderRepo.save(od);
    }

    private static String genOrderCode(){
        return "OD" + RandomStringUtils.randomAlphanumeric(10).toUpperCase();
    }

    private static BigDecimal unitPriceFromCartOrTicket(Object cartLine, TicketType tt){
        try {
            var f = cartLine.getClass().getDeclaredField("unitPrice");
            f.setAccessible(true);
            Object v = f.get(cartLine);
            if (v instanceof BigDecimal bd) return bd;
            if (v instanceof Long l) return BigDecimal.valueOf(l);
            if (v instanceof Integer i) return BigDecimal.valueOf(i);
        } catch (Exception ignore) {}
        try {
            var f = cartLine.getClass().getDeclaredField("price");
            f.setAccessible(true);
            Object v = f.get(cartLine);
            if (v instanceof BigDecimal bd) return bd;
            if (v instanceof Long l) return BigDecimal.valueOf(l);
            if (v instanceof Integer i) return BigDecimal.valueOf(i);
        } catch (Exception ignore) {}
        return tt.getPrice() != null ? tt.getPrice() : BigDecimal.ZERO;
    }
}
