package com.example.E_Ticket.service.impl;


import com.example.E_Ticket.dto.OrderCreateReq;
import com.example.E_Ticket.entity.*;
import com.example.E_Ticket.exception.BusinessException;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.mapper.OrderMapper;
import com.example.E_Ticket.repository.*;
import com.example.E_Ticket.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final TicketTypeRepository ticketTypeRepository;
    private final OrderItemRepository orderItemRepository;

    private static final int RETRIES = 3;


    @Override
    public Page<Order> list(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Override
    public Order getByCode(String code) {
        return orderRepository.findOrderByCode(code).orElseThrow(() -> new NotFoundException("Order Not Found"));
    }

    @Override
    public Optional<Order> findByCode(String code) {
        return orderRepository.findOrderByCode(code);
    }

    @Override
    @Transactional
    public Order placeOrder(OrderCreateReq req, Long userId, boolean markPaid){
        Event event = eventRepository.findById(req.eventId()).orElseThrow(() -> new NotFoundException("Event not found"));
        User user = (userId==null)? null : userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

        int attempt = 0;
        while (true){
            try {
                return createOrderInternal(req, event, user, markPaid);
            } catch (OptimisticLockingFailureException e){
                if (++attempt >= RETRIES) throw new BusinessException("Hệ thống bận, vui lòng thử lại.");
                // retry
            }
        }
    }
    private Order createOrderInternal(OrderCreateReq req, Event event, User user, boolean markPaid){
        BigDecimal total = BigDecimal.ZERO;
        List<OrderItem> items = new ArrayList<>();

        for (OrderCreateReq.Item it : req.items()){
            TicketType tt = ticketTypeRepository.findById(it.ticketTypeId())
                    .orElseThrow(() -> new NotFoundException("Ticket type not found"));
            if (!tt.getEvent().getId().equals(event.getId()))
                throw new BusinessException("Ticket không thuộc sự kiện này");

            int need = it.qty();
            if (need <= 0) throw new BusinessException("Số lượng không hợp lệ");

            // Optimistic lock bằng @Version
            if (tt.getSold() + need > tt.getQuota())
                throw new BusinessException("Hết vé: " + tt.getName());

            tt.setSold(tt.getSold() + need); // sẽ update lúc flush; nếu cạnh tranh -> OptimisticLock
            total = total.add(tt.getPrice().multiply(BigDecimal.valueOf(need)));

            OrderItem oi = new OrderItem();
            oi.setTicketType(tt);
            oi.setQty(need);
            oi.setPrice(tt.getPrice());
            items.add(oi);
        }

        Order order = new Order();
        order.setCode(genCode());
        order.setUser(user);
        order.setEvent(event);
        order.setTotal(total);
        order.setPaymentMethod(req.paymentMethod());
        order.setStatus(markPaid ? Order.Status.PAID : Order.Status.PENDING);
        order = orderRepository.save(order);

        for (OrderItem oi : items){
            oi.setOrder(order);
            orderItemRepository.save(oi);
            order.getItems().add(oi);
        }
        return order;
    }

    @Transactional
    public void cancel(String code){
        Order o = getByCode(code);
        if (o.getStatus()==Order.Status.CANCELLED) return;
        // hoàn vé về quota
        for (OrderItem i : o.getItems()){
            TicketType tt = i.getTicketType();
            tt.setSold(tt.getSold() - i.getQty());
        }
        o.setStatus(Order.Status.CANCELLED);
    }

    private static final SecureRandom RND = new SecureRandom();
    private String genCode(){
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("NX-");
        for (int i=0;i<6;i++) sb.append(alphabet.charAt(RND.nextInt(alphabet.length())));
        return sb.toString();
    }
}
