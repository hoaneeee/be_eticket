package com.example.E_Ticket.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserUpserReq(
        @Email @NotBlank String email,
        @NotBlank String password,
        String fullname,
        String role,
        Boolean enable
) {}
