package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.entity.Event;
import com.example.E_Ticket.entity.SeatZone;
import com.example.E_Ticket.repository.EventRepository;
import com.example.E_Ticket.repository.SeatZoneRepository;
import com.example.E_Ticket.repository.SoldSeatRepository;
import com.example.E_Ticket.repository.ZonePriceRepository;
import com.example.E_Ticket.service.impl.CartSessionService;
import com.example.E_Ticket.service.impl.InventorySeatHoldService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/public/v1/cart")
@RequiredArgsConstructor
public class CartSeatPublicApi {
    private final InventorySeatHoldService holdService;
    private final SeatZoneRepository zoneRepository;
    private final EventRepository eventRepository;
    private final ZonePriceRepository zonePriceRepository;
    private final SoldSeatRepository soldSeatRepository;
    private final CartSessionService cartSessionService;

    public record HoldSeatReq(Long eventId, Long zoneId, Integer seatNo) {}
    public record HoldSeatRes(boolean ok, String message, Instant expiresAt) {}

    @PostMapping("/hold-seat")
    public HoldSeatRes holdSeat(@RequestBody HoldSeatReq req, HttpSession session) {
        if (req.eventId() == null || req.zoneId() == null || req.seatNo() == null) {
            return new HoldSeatRes(false, "thiếu tham số", null);
        }

        Optional<Event> evOpt = eventRepository.findById(req.eventId());
        if (evOpt.isEmpty()) return new HoldSeatRes(false, "event not found", null);
        Event event = evOpt.get();
        if (event.getSeatMap() == null) return new HoldSeatRes(false, "event chưa gắn seat map", null);

        Optional<SeatZone> zoneOpt = zoneRepository.findById(req.zoneId());
        if (zoneOpt.isEmpty()) return new HoldSeatRes(false, "zone not found", null);
        SeatZone zone = zoneOpt.get();

        if (!zone.getSeatMap().getId().equals(event.getSeatMap().getId())) {
            return new HoldSeatRes(false, "zone không thuộc seat map của event", null);
        }

        // CHẶN ghế đã bán
        if (soldSeatRepository.existsByEvent_IdAndZone_IdAndSeatNo(req.eventId(), req.zoneId(), req.seatNo())) {
            return new HoldSeatRes(false, "ghế đã bán", null);
        }

        try {
            var hold = holdService.tryHold(req.eventId(), req.zoneId(), req.seatNo(), session.getId());
            if (hold == null) return new HoldSeatRes(false, "không thể giữ ghế", null);

            var zp = zonePriceRepository
                    .findFirstByEvent_IdAndSeatZone_Id(req.eventId(), req.zoneId())
                    .orElseThrow(() -> new IllegalStateException("Chưa cấu hình zone_prices cho zone"));

            long price = zp.getPrice().longValue();
            String zoneName = zone.getName();

            cartSessionService.addSeat(session, req.eventId(), zp.getTicketType().getId(),
                    "Zone " + zoneName + " – Ghế " + req.seatNo(), price, 1);

            return new HoldSeatRes(true, "đã giữ ghế", hold.getExpiresAt());
        } catch (Exception e) {
            return new HoldSeatRes(false, "lỗi hệ thống khi giữ ghế", null);
        }
    }

    @PostMapping("/release-seat")
    public Map<String, Object> releaseSeat(@RequestBody HoldSeatReq req, HttpSession session) {
        if (req.eventId() == null || req.zoneId() == null || req.seatNo() == null) {
            return Map.of("ok", false, "message", "thiếu tham số");
        }
        try {
            holdService.release(req.eventId(), req.zoneId(), req.seatNo(), session.getId());
            String name = "Zone " + zoneRepository.findById(req.zoneId()).map(SeatZone::getName).orElse("?")
                    + " – Ghế " + req.seatNo();
            cartSessionService.removeSeatByName(session, name);
            return Map.of("ok", true);
        } catch (Exception e) {
            return Map.of("ok", false, "message", "lỗi hệ thống khi bỏ ghế");
        }
    }
    @GetMapping("/holds")
    public Map<String,Object> holds(@RequestParam Long eventId, HttpSession session){
        var list = holdService.snapshotByEvent(eventId);
        // return mine=true if session hien tai ->  “xanh dang chon”, con lai “vàng”
        var out = list.stream().map(h -> Map.of(
                "zoneId", h.getZoneId(),
                "seatNo", h.getSeatNo(),
                "mine",  Objects.equals(h.getSessionId(), session.getId())
        )).toList();
        return Map.of("holds", out);
    }
}
