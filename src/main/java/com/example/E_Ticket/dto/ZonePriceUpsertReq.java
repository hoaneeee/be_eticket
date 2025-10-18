package com.example.E_Ticket.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record ZonePriceUpsertReq(@NotNull Long eventId,
                                 @NotNull Long ticketTypeId,
                                 @NotNull Long seatZoneId,
                                 @NotNull BigDecimal price) {
}
