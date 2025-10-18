package com.example.E_Ticket.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class AuthDtos {
    public record RegisterForm(
            @NotBlank String fullName,
            @NotBlank @Email String email,
            @NotBlank String password
    ) {}
}

