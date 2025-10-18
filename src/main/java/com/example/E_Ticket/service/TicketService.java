package com.example.E_Ticket.service;

import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.entity.Ticket;

import java.util.List;

public interface TicketService {
    List<Ticket> issueTickets(Order order);

    Ticket checkIn (String ticketCodeOrPayload);
}
