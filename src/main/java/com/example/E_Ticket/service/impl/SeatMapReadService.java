package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.entity.Event;
import com.example.E_Ticket.entity.SeatMap;
import com.example.E_Ticket.entity.SeatZone;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.repository.EventRepository;
import com.example.E_Ticket.repository.SeatMapRepository;
import com.example.E_Ticket.repository.SeatZoneRepository;
import com.example.E_Ticket.repository.SoldSeatRepository;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatMapReadService {
    private static final Logger log = LoggerFactory.getLogger(SeatMapReadService.class);

    private final EventRepository eventRepo;
    private final SeatMapRepository mapRepo;
    private final SeatZoneRepository zoneRepo;
    private final SoldSeatRepository soldSeatRepo;

    public SeatPack loadForEventSlug(String slug) {
        Event e = eventRepo.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (e.getSeatMap() == null) throw new NotFoundException("Event has no seat map");

        SeatMap m = mapRepo.findById(e.getSeatMap().getId())
                .orElseThrow(() -> new NotFoundException("Seat map not found"));
        List<SeatZone> zones = zoneRepo.findBySeatMap_Id(m.getId());

        String svg = readSvgText(m.getSvgPath());

        java.util.Set<String> soldKeys = soldSeatRepo.findByEvent_Id(e.getId()).stream()
                .map(s -> s.getZone().getId() + ":" + s.getSeatNo())
                .collect(java.util.stream.Collectors.toSet());

        return new SeatPack(e, m, zones, svg, soldKeys);
    }

    private String readSvgText(@Nullable String pathStr) {
        if (pathStr == null || pathStr.isBlank()) return "";
        try {
            String normalized = pathStr;
            if (normalized.startsWith("/uploads/")) normalized = normalized.substring(1);
            Path disk = Paths.get(normalized);
            if (!Files.exists(disk) && normalized.startsWith("uploads")) {
                disk = Paths.get(System.getProperty("user.dir")).resolve(normalized);
            }
            return Files.exists(disk) ? Files.readString(disk) : "";
        } catch (Exception ex) {
            log.warn("Cannot read SVG from {}: {}", pathStr, ex.toString());
            return "";
        }
    }

    public record SeatPack(
            Event event,
            SeatMap map,
            List<SeatZone> zones,
            String inlineSvg,
            java.util.Set<String> soldKeys
    ) {}
}
