package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.entity.CheckIn;
import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.exception.BusinessException;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.repository.CheckinRepository;
import com.example.E_Ticket.repository.OrderRepository;
import com.example.E_Ticket.service.CheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckInService {
    private final CheckinRepository checkinRepo;
    private final OrderRepository orderRepo;

    @Override
    public CheckIn verifyAndCheckin(String code, Long scannedBy) {
        // 1) Tìm đơn theo code
        Order order = orderRepo.findOrderByCode(code)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        // 2) Phải là PAID (tuỳ kiểu status của bạn)
        // Nếu status là String:
        if (order.getStatus() != Order.Status.PAID) {
            throw new BusinessException("Order is not PAID");
        }
        // Nếu status là Enum: if (order.getStatus() != Order.Status.PAID) { ... }

        // 3) Nếu đã có checkin -> fail sớm (tránh save thừa)
        checkinRepo.findByCode(code).ifPresent(ci -> {
            throw new BusinessException("This code was already checked in at " + ci.getScannedAt());
        });

        // 4) Tạo check-in + gán order BẮT BUỘC + chống race
        try {
            CheckIn ck = new CheckIn();
            ck.setCode(code);
            ck.setOrder(order);                 // BẮT BUỘC
            ck.setScannedAt(Instant.now());
            ck.setScannedBy(scannedBy);         // null nếu chưa truyền
            return checkinRepo.save(ck);
        } catch (DataIntegrityViolationException dup) {
            // Trường hợp 2 request đua nhau, unique index nổ:
            var existed = checkinRepo.findByCode(code).orElse(null);
            var when = existed != null ? existed.getScannedAt() : null;
            throw new BusinessException("This code was already checked in" + (when != null ? (" at " + when) : ""));
        }
    }
}
