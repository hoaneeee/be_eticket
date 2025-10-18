package com.example.E_Ticket.dto;

public record InventoryConfigDto(
        Long id,
        Long eventId,
        Integer holdTimeoutSec,
        Boolean allowOverbook,
        Integer maxRenewPerHold
) {}