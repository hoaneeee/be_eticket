package com.example.E_Ticket.dto;

import jakarta.validation.constraints.NotBlank;

public record CheckinCreateReq(
        @NotBlank String code // mã QR/Hash
) {
}
