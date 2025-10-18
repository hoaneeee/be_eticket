package com.example.E_Ticket.service;

import com.example.E_Ticket.entity.User;

public interface VerificationService {

    void createAndSend(User user);
    String verify(String token);
    void resendVerification(String token);
}
