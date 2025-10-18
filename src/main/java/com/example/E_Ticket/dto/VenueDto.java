package com.example.E_Ticket.dto;

public record VenueDto(
        Long id, String name, String address,
        Integer capacity, String description, String imageUrl
) {
}
