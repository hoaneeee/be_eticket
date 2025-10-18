package com.example.E_Ticket.dto;


import jakarta.validation.constraints.NotBlank;

public record SeatZoneUpsertReq(
        @NotBlank String code,

        @NotBlank String name,

        Integer capacity,
        @NotBlank String polygon) {
}
