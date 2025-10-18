package com.example.E_Ticket.dto;


public record InventoryConfigUpsertReq(
        Integer holdTimeoutSec,
        Boolean allowOverbook,
        Integer maxRenewPerHold
) {}