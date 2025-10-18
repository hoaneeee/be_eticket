
package com.example.E_Ticket.service.impl;

import com.example.E_Ticket.dto.SeatMapDto;
import com.example.E_Ticket.entity.SeatMap;
import com.example.E_Ticket.entity.Venue;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.mapper.SeatMapMapper;
import com.example.E_Ticket.repository.SeatMapRepository;
import com.example.E_Ticket.repository.VenueRepository;
import com.example.E_Ticket.service.SeatMapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatMapServiceImpl implements SeatMapService {

    private final VenueRepository venueRepo;
    private final SeatMapRepository seatMapRepo;

    @Override
    @Transactional
    public SeatMapDto uploadSeatMap(Long venueId, MultipartFile svgFile, String name) throws IOException {
        var venue = venueRepo.findById(venueId).orElseThrow(() -> new NotFoundException("Venue not found"));
        if (svgFile == null || svgFile.isEmpty()) throw new IllegalArgumentException("Empty file");
        var contentType = svgFile.getContentType();
        if (contentType != null && !contentType.contains("svg")) {
            throw new IllegalArgumentException("Only SVG is accepted");
        }

        Path dir = Paths.get("uploads");
        Files.createDirectories(dir);
        String filename = System.currentTimeMillis() + "-" + UUID.randomUUID() + ".svg";
        Path dest = dir.resolve(filename);
        Files.copy(svgFile.getInputStream(), dest, StandardCopyOption.REPLACE_EXISTING);

        var map = seatMapRepo.save(SeatMap.builder()
                .venue(venue)
                .name((name == null || name.isBlank()) ? ("SeatMap " + venue.getName()) : name)
                .svgPath("/uploads/" + filename)
                .build());

        return SeatMapMapper.toDto(map);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SeatMapDto> listByVenue(Long venueId) {
        return seatMapRepo.findByVenue_IdOrderByIdDesc(venueId)
                .stream().map(SeatMapMapper::toDto).toList();
    }
}


