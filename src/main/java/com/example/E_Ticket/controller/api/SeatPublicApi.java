package com.example.E_Ticket.controller.api;

import com.example.E_Ticket.repository.SoldSeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/public/v1/seats")
@RequiredArgsConstructor
public class SeatPublicApi {

    private final SoldSeatRepository soldSeatRepo;

    // JS của bạn đang poll endpoint này
    @GetMapping("/sold-keys")
    public List<String> soldKeys(@RequestParam Long eventId) {
        return soldSeatRepo.keysByEvent(eventId);
    }
}