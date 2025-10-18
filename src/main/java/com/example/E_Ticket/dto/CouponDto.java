package com.example.E_Ticket.dto;

import java.time.Instant;

public record CouponDto(
        Long id, String code, String type, Long value,
        Instant startAt, Instant endAt, Integer maxUse, Integer perUserLimit, Integer used
) {
}
