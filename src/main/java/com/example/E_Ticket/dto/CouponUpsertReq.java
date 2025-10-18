package com.example.E_Ticket.dto;

import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.Instant;

public record CouponUpsertReq(
        @NotBlank String code,
        @NotBlank String type,                // PERCENT | AMOUNT
        @NotNull  @Positive Long value,
        Instant startAt,
        Instant endAt,
        @PositiveOrZero Integer maxUse,
        @PositiveOrZero Integer perUserLimit
) {
    @JsonIgnore
    public boolean isValidWindow() {
        return startAt == null || endAt == null || endAt.isAfter(startAt);
    }
}