package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.dto.SeatZoneDto;
import com.example.E_Ticket.dto.SeatZoneUpsertReq;
import com.example.E_Ticket.entity.SeatMap;
import com.example.E_Ticket.entity.SeatZone;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.mapper.SeatZoneMapper;
import com.example.E_Ticket.repository.SeatMapRepository;
import com.example.E_Ticket.repository.SeatZoneRepository;
import com.example.E_Ticket.service.SeatZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatZoneServiceImpl implements SeatZoneService {

    private final SeatMapRepository seatMapRepo;
    private final SeatZoneRepository seatZoneRepo;

    @Override
    @Transactional(readOnly = true)
    public List<SeatZoneDto> listByMap(Long seatMapId) {
        return seatZoneRepo.findBySeatMap_IdOrderByIdAsc(seatMapId).stream().map(SeatZoneMapper::toDto).toList();
    }

    @Override
    @Transactional
    public SeatZoneDto create(Long seatMapId, SeatZoneUpsertReq r) {
        validatePolygon(r.polygon());
        SeatMap map = seatMapRepo.findById(seatMapId).orElseThrow(() -> new NotFoundException("SeatMap not found"));
        var z = SeatZone.builder()
                .seatMap(map).code(r.code()).name(r.name()).capacity(r.capacity()).polygon(r.polygon())
                .build();
        try {
            return SeatZoneMapper.toDto(seatZoneRepo.save(z));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Zone code already exists on this seat map");
        }
    }

    @Override
    @Transactional
    public SeatZoneDto update(Long zoneId, SeatZoneUpsertReq r) {
        validatePolygon(r.polygon());
        var z = seatZoneRepo.findById(zoneId).orElseThrow(() -> new NotFoundException("SeatZone not found"));
        z.setCode(r.code()); z.setName(r.name()); z.setCapacity(r.capacity()); z.setPolygon(r.polygon());
        try {
            return SeatZoneMapper.toDto(seatZoneRepo.save(z));
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("Zone code already exists on this seat map");
        }
    }

    @Override
    @Transactional
    public void delete(Long zoneId) {
        seatZoneRepo.deleteById(zoneId);
    }

    private void validatePolygon(String polygon) {
        if (polygon == null || polygon.isBlank() || polygon.trim().split("\\s+").length < 3) {
            throw new IllegalArgumentException("Polygon must include at least 3 points: \"x,y x,y x,y\"");
        }
    }
}
