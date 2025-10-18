package com.example.E_Ticket.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.eclipse.angus.mail.imap.protocol.Item;

import java.util.List;

public record OrderCreateReq(
        @NotNull Long eventId,
        @NotEmpty List<Item> items,
        String paymentMethod // COD|BANK|MOMO (mock)
) {
    public record Item(@NotNull Long ticketTypeId, @NotNull @Min(1) Integer qty) {}
}
