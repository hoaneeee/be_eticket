package com.example.E_Ticket.controller.api;

import com.example.E_Ticket.dto.ZonePriceDto;
import com.example.E_Ticket.dto.ZonePriceUpsertReq;
import com.example.E_Ticket.service.ZonePriceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/zone-prices")
@RequiredArgsConstructor
public class ZonePriceAdminApi {

    private final ZonePriceService zonePriceService;

    @GetMapping("/by-event/{eventId}")
    public List<ZonePriceDto> list(@PathVariable Long eventId){
        return zonePriceService.listByEvent(eventId);
    }

    @PostMapping
    public ZonePriceDto upsert(@RequestBody ZonePriceUpsertReq r){
        return zonePriceService.upsert(r);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){
        zonePriceService.delete(id);
    }
}
