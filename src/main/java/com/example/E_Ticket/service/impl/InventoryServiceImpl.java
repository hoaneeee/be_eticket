
package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.dto.*;
import com.example.E_Ticket.entity.*;
import com.example.E_Ticket.entity.TicketHold.Status;
import com.example.E_Ticket.exception.BusinessException;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.mapper.InventoryMapper;
import com.example.E_Ticket.mapper.TicketHoldMapper;
import com.example.E_Ticket.repository.*;
import com.example.E_Ticket.service.InventoryService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {
    private final InventoryConfigRepository configRepo;
    private final EventRepository eventRepo;
    private final TicketTypeRepository ticketTypeRepo;
    private final TicketHoldRepository holdRepo;

    @Override
    public InventoryConfigDto getConfig(Long eventId) {
        var cfg = configRepo.findByEvent_Id(eventId).orElseGet(() -> {
            var e = eventRepo.findById(eventId)
                    .orElseThrow(() -> new NotFoundException("Event not found"));
            return configRepo.save(InventoryConfig.builder().event(e).build());
        });
        return InventoryMapper.toDto(cfg);
    }

    @Override
    @Transactional
    public InventoryConfigDto upsertConfig(Long eventId, InventoryConfigUpsertReq req) {
        var cfg = configRepo.findByEvent_Id(eventId).orElseGet(() -> {
            var e = eventRepo.findById(eventId)
                    .orElseThrow(() -> new NotFoundException("Event not found"));
            return InventoryConfig.builder().event(e).build();
        });
        if (req.holdTimeoutSec() != null) cfg.setHoldTimeoutSec(req.holdTimeoutSec());
        if (req.allowOverbook() != null) cfg.setAllowOverbook(req.allowOverbook());
        if (req.maxRenewPerHold() != null) cfg.setMaxRenewPerHold(req.maxRenewPerHold());
        return InventoryMapper.toDto(configRepo.save(cfg));
    }

    @Override
    @Transactional
    public TicketHoldDto createHold(TicketHoldCreateReq r) {
        var t = ticketTypeRepo.findById(r.ticketTypeId())
                .orElseThrow(() -> new NotFoundException("TicketType not found"));
        var cfg = getConfig(r.eventId());

        // tong da giu ACTIVE
        var now = Instant.now();
        var activeHolds = holdRepo.findByTicketType_IdAndStatusAndExpiresAtAfter(t.getId(), Status.ACTIVE, now);
        int holdingQty = activeHolds.stream().mapToInt(TicketHold::getQty).sum();

        // da ban
        int sold = t.getSold() == null ? 0 : t.getSold();

        int available = t.getQuota() - sold - holdingQty;
        if (!cfg.allowOverbook() && r.qty() > available) {
            throw new BusinessException("Not enough tickets available");
        }

        var expires = now.plusSeconds(cfg.holdTimeoutSec());
        var hold = TicketHold.builder()
                .event(t.getEvent())
                .ticketType(t)
                .qty(r.qty())
                .userId(r.userId())
                .sessionId(r.sessionId())
                .expiresAt(expires)
                .status(Status.ACTIVE)
                .build();

        return TicketHoldMapper.toDto(holdRepo.save(hold));
    }

    @Override
    @Transactional
    public void releaseHold(Long holdId) {
        var h = holdRepo.findByIdAndStatus(holdId, Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Hold not found or not active"));
        h.setStatus(Status.RELEASED);
        holdRepo.save(h);
    }

    @Override
    @Transactional
    public TicketHoldDto renewHold(Long holdId) {
        var h = holdRepo.findByIdAndStatus(holdId, TicketHold.Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Hold not found or not active"));

        // nếu đã hết hạn thì convert sang EXPIRED luôn
        if (h.getExpiresAt().isBefore(Instant.now())) {
            h.setStatus(TicketHold.Status.EXPIRED);
            holdRepo.save(h);
            throw new BusinessException("Hold expired");
        }

        // Lấy ngưỡng (hoặc đặt cố định = 1)
        int max = getConfig(h.getEvent().getId()).maxRenewPerHold();  // or: int max = 1;

        if (h.getRenewCount() >= max) {
            h.setStatus(TicketHold.Status.RELEASED);
            holdRepo.save(h);
            throw new BusinessException("RENEW_LIMIT");
        }

        // cho renew
        var cfg = getConfig(h.getEvent().getId());
        h.setRenewCount(h.getRenewCount() + 1);
        h.setExpiresAt(Instant.now().plusSeconds(cfg.holdTimeoutSec()));
        return TicketHoldMapper.toDto(holdRepo.save(h));
    }

    @Override
    @Transactional
    public void consumeHold(Long holdId) {
        var h = holdRepo.findByIdAndStatus(holdId, TicketHold.Status.ACTIVE)
                .orElseThrow(() -> new NotFoundException("Hold not found or not active"));
        h.setStatus(TicketHold.Status.CONSUMED);
        holdRepo.save(h);

        var t = h.getTicketType();
        int sold = (t.getSold() == null ? 0 : t.getSold());
        t.setSold(sold + h.getQty());
        ticketTypeRepo.save(t);
    }

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void cleanupExpired() {
        var now = Instant.now();
        holdRepo.findByStatusAndExpiresAtBefore(Status.ACTIVE, now)
                .forEach(h -> {
                    h.setStatus(Status.EXPIRED);
                    holdRepo.save(h);
                });
    }
    @Override
    public boolean isHoldActive(Long holdId){
        return holdRepo.findByIdAndStatus(holdId, TicketHold.Status.ACTIVE)
                .filter(h -> h.getExpiresAt().isAfter(Instant.now()))
                .isPresent();
    }

    @Override
    @Transactional
    public void consumeAllActiveBySessionAndEvent(String sessionId, Long eventId) {
        if (sessionId == null) return;
        var holds = holdRepo.findByEvent_IdAndSessionIdAndStatus(eventId, sessionId, TicketHold.Status.ACTIVE);
        for (var h : holds) {
            h.setStatus(TicketHold.Status.CONSUMED);
            holdRepo.save(h);

            var tt = h.getTicketType();
            int sold = (tt.getSold() == null ? 0 : tt.getSold());
            tt.setSold(sold + h.getQty());
            ticketTypeRepo.save(tt);
        }
    }
}
