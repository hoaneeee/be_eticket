package com.example.E_Ticket.dto;

import java.time.Instant;

public record EventDto (
        Long id,
        String title,
        String slug,
        Instant eventDate,
        String status,
        String bannerUrl,
        Long venueId,
        String venueName,
        String venueAddress,
        Long seatMapId,
        String description
){ }
