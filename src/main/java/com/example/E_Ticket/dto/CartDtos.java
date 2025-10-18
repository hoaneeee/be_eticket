package com.example.E_Ticket.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class CartDtos {
    /** payload POST /cart/add */
    public record AddToCartReq(
            @NotEmpty List<Item> items
    ) {
        public record Item(
                @NotNull Long ticketTypeId,
                @Min(1) int qty
        ) {}
    }
}