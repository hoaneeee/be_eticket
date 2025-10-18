package com.example.E_Ticket.controller.api;

import com.example.E_Ticket.dto.SeatMapDto;
import com.example.E_Ticket.dto.SeatZoneDto;
import com.example.E_Ticket.dto.SeatZoneUpsertReq;
import com.example.E_Ticket.service.SeatMapService;
import com.example.E_Ticket.service.SeatZoneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/admin/v1")
@RequiredArgsConstructor
public class SeatMapAdminApi {

    private final SeatMapService seatMapService;
    private final SeatZoneService seatZoneService;



    @PostMapping(path = "/venues/{venueId}/seatmap", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public SeatMapDto createSeatMap(@PathVariable Long venueId,
                                    @RequestPart("file") MultipartFile file,
                                    @RequestPart(value = "name", required = false) String name) throws IOException {
        return seatMapService.uploadSeatMap(venueId, file, name);
    }

    @GetMapping("/seatmaps/by-venue/{venueId}")
    public List<SeatMapDto> listByVenue(@PathVariable Long venueId){
        return seatMapService.listByVenue(venueId);
    }

    @GetMapping("/seatmaps/{mapId}/zones")
    public List<SeatZoneDto> zones(@PathVariable Long mapId){
        return seatZoneService.listByMap(mapId);
    }

    @PostMapping("/seatmaps/{mapId}/zones")
    public SeatZoneDto addZone(@PathVariable Long mapId, @RequestBody SeatZoneUpsertReq r){
        return seatZoneService.create(mapId, r);
    }

    @PutMapping("/seatmaps/zones/{id}")
    public SeatZoneDto updateZone(@PathVariable Long id, @RequestBody SeatZoneUpsertReq r){
        return seatZoneService.update(id, r);
    }

    @DeleteMapping("/seatmaps/zones/{id}")
    public void deleteZone(@PathVariable Long id){
        seatZoneService.delete(id);
    }
}
