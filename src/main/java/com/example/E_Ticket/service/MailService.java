package com.example.E_Ticket.service;

public interface MailService {
    void send(String to, String subject, String htmlBody);
}
