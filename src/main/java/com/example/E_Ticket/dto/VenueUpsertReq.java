package com.example.E_Ticket.dto;

import jakarta.validation.constraints.NotBlank;

public record VenueUpsertReq(
        @NotBlank String name,
        @NotBlank String address,
        Integer capacity,
        String description,
        String imageUrl
) {
}
