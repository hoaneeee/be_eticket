package com.example.E_Ticket.dto;

import java.time.Instant;

public record UserDTO (
    Long id,
    String email,
    String fullname,
    String role,
    Boolean enabled,
    Instant createAt
    ) { }
