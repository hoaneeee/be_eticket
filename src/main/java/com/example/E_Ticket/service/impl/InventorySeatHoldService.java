package com.example.E_Ticket.service.impl;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InventorySeatHoldService {
    private final Map<String, Hold> holds = new ConcurrentHashMap<>();
    private final long ttlSeconds = 120; // 5 phút

    public synchronized Hold tryHold(Long eventId, Long zoneId, Integer seatNo, String sessionId){
        String key = key(eventId, zoneId, seatNo);
        cleanupExpired();

        Hold h = holds.get(key);
        if (h != null && !h.isExpired() && !Objects.equals(h.sessionId, sessionId)) {
            throw new IllegalStateException("Ghế đang được giữ bởi người khác.");
        }
        Hold newHold = new Hold(eventId, zoneId, seatNo, sessionId, Instant.now().plusSeconds(ttlSeconds));
        holds.put(key, newHold);
        return newHold;
    }

    public synchronized void release(Long eventId, Long zoneId, Integer seatNo, String sessionId){
        String key = key(eventId, zoneId, seatNo);
        Hold h = holds.get(key);
        if (h != null && Objects.equals(h.sessionId, sessionId)) {
            holds.remove(key);
        }
    }

    public boolean isHeld(Long eventId, Long zoneId, Integer seatNo){
        Hold h = holds.get(key(eventId, zoneId, seatNo));
        return h != null && !h.isExpired();
    }

    public synchronized java.util.List<Hold> snapshotByEventAndSession(Long eventId, String sessionId){
        cleanupExpired();
        return holds.values().stream()
                .filter(h -> h.eventId.equals(eventId) && Objects.equals(h.sessionId, sessionId) && !h.isExpired())
                .sorted(java.util.Comparator.comparing(h -> h.expiresAt))
                .toList();
    }

    public synchronized void clear(Long eventId, Long zoneId, Integer seatNo){
        holds.remove(key(eventId, zoneId, seatNo));
    }

    private String key(Long e, Long z, Integer s){ return e + ":" + z + ":" + s; }

    private void cleanupExpired(){
        Instant now = Instant.now();
        holds.values().removeIf(h -> h.expiresAt.isBefore(now));
    }

    public synchronized java.util.List<Hold> snapshotByEvent(Long eventId){
        cleanupExpired();
        return holds.values().stream()
                .filter(h -> h.eventId.equals(eventId) && !h.isExpired())
                .toList();
    }

    @Data @AllArgsConstructor
    public static class Hold {
        public Long eventId;
        public Long zoneId;
        public Integer seatNo;
        public String sessionId;
        public Instant expiresAt;
        public boolean isExpired(){ return expiresAt.isBefore(Instant.now()); }
    }
}
