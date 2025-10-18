package com.example.E_Ticket.controller.api;

import com.example.E_Ticket.dto.TicketHoldCreateReq;
import com.example.E_Ticket.dto.TicketHoldDto;
import com.example.E_Ticket.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/v1/inventory")
@RequiredArgsConstructor
public class InventoryPublicApi {
    private final InventoryService service;

    @PostMapping("/holds")
    public TicketHoldDto hold(@RequestBody TicketHoldCreateReq req){
        return service.createHold(req);
    }

    @PostMapping("/holds/{id}/release")
    public void release(@PathVariable Long id){
        service.releaseHold(id);
    }

    @PostMapping("/holds/{id}/consume")
    public void consume(@PathVariable Long id){
        service.consumeHold(id);
    }
    @PostMapping("/holds/{id}/renew")
    public TicketHoldDto renew(@PathVariable Long id){
        return service.renewHold(id);
    }
}
