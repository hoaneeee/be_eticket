// src/main/java/com/example/E_Ticket/controller/api/CheckinApi.java
package com.example.E_Ticket.controller.api;

import com.example.E_Ticket.entity.Ticket;
import com.example.E_Ticket.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/checkin")
@RequiredArgsConstructor
public class CheckinApi {
    private final TicketService ticketService;

    @PostMapping
    public Map<String,Object> checkin(@RequestParam String code) {
        Ticket t = ticketService.checkIn(code); // nhận code hoặc payload
        return Map.of(
                "ticketId", t.getId(),
                "code", t.getCode(),
                "status", t.getStatus().name(),
                "orderCode", t.getOrder().getCode()
        );
    }
}
