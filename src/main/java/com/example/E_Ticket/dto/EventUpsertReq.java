package com.example.E_Ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDateTime;

public record EventUpsertReq(
        @NotBlank String title,
        @NotBlank String slug,
        @NotNull Instant eventDate,
        @NotBlank String status,     // DRAFT/PUBLISHED
        Long venueId,                // cho phep null
        String bannerUrl,
        String description,
        Long seatMapId
) {
}
