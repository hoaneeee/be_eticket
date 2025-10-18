// src/main/java/com/example/E_Ticket/controller/api/TicketTypeAdminApi.java
package com.example.E_Ticket.controller.api;

import com.example.E_Ticket.dto.TicketTypeDto;
import com.example.E_Ticket.dto.TicketTypeUpsertReq;
import com.example.E_Ticket.entity.TicketType;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.mapper.TicketTypeMapper;
import com.example.E_Ticket.service.TicketTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/tickets")
@RequiredArgsConstructor
public class TicketTypeAdminApi {

    private final TicketTypeService ticketTypeService;

    @GetMapping("/by-event/{eventId}")
    public List<TicketTypeDto> findByEventId(@PathVariable Long eventId) {
        return ticketTypeService.listByEvent(eventId).stream()
                .map(TicketTypeMapper::toDto)
                .toList();
    }

    @PostMapping
    public TicketTypeDto create (@RequestBody @Valid TicketTypeUpsertReq req) {
        TicketType t = TicketType.builder()
                .name(req.name())
                .price(req.price())
                .quota(req.quota())
                .build();
        return TicketTypeMapper.toDto(ticketTypeService.create(req.eventId(), t));
    }

    @PutMapping("/{id}")
    public TicketTypeDto update(@PathVariable Long id, @RequestBody @Valid TicketTypeUpsertReq req){
        TicketType patch = new TicketType();
        patch.setName(req.name());
        patch.setPrice(req.price());
        patch.setQuota(req.quota());
        return TicketTypeMapper.toDto(ticketTypeService.update(id, req.eventId(), patch));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        ticketTypeService.delete(id);
    }
}
