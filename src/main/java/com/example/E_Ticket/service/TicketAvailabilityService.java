package com.example.E_Ticket.service;

import java.util.List;

public interface TicketAvailabilityService {
    record Item(Long ticketTypeId, String ticketTypeName, int available) {}

    List<Item> availableByTicketType(Long eventId);
}