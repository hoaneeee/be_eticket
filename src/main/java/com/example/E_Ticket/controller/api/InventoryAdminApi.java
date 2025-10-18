package com.example.E_Ticket.controller.api;

import com.example.E_Ticket.dto.InventoryConfigDto;
import com.example.E_Ticket.dto.InventoryConfigUpsertReq;
import com.example.E_Ticket.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/v1/inventory")
@RequiredArgsConstructor
public class InventoryAdminApi {
    private final InventoryService service;

    @GetMapping("/events/{eventId}/config")
    public InventoryConfigDto get(@PathVariable Long eventId){
        return service.getConfig(eventId);
    }

    @PutMapping("/events/{eventId}/config")
    public InventoryConfigDto upsert(@PathVariable Long eventId,
                                     @RequestBody InventoryConfigUpsertReq req){
        return service.upsertConfig(eventId, req);
    }
}
