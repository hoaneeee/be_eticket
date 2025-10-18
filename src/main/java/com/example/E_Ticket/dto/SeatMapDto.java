package com.example.E_Ticket.dto;

public record SeatMapDto(
        Long id, Long venueId,
        String name, String svgPath) {
}
