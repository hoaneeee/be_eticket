package com.example.E_Ticket.dto;

import java.math.BigDecimal;

public record ZonePriceDto(Long id, Long eventId, Long ticketTypeId, Long seatZoneId, BigDecimal price) {
}
