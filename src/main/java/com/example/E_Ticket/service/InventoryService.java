package com.example.E_Ticket.service;

import com.example.E_Ticket.dto.InventoryConfigDto;
import com.example.E_Ticket.dto.InventoryConfigUpsertReq;
import com.example.E_Ticket.dto.TicketHoldCreateReq;
import com.example.E_Ticket.dto.TicketHoldDto;

public interface InventoryService {
    InventoryConfigDto getConfig(Long eventId);
    InventoryConfigDto upsertConfig(Long eventId, InventoryConfigUpsertReq req);

    TicketHoldDto createHold(TicketHoldCreateReq req);
    void releaseHold(Long holdId);     // user cancel / timeout clean
    void consumeHold(Long holdId);     // create successfully
    void cleanupExpired();             // clean

    TicketHoldDto renewHold(Long holdId);
    boolean isHoldActive(Long holdId);


    void consumeAllActiveBySessionAndEvent(String sessionId, Long eventId);
}