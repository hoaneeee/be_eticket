package com.example.E_Ticket.service;

import com.example.E_Ticket.dto.SeatMapDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface SeatMapService {
    SeatMapDto uploadSeatMap(Long venueId, MultipartFile svgFile, String name) throws IOException;
    List<SeatMapDto> listByVenue(Long venueId);
}
