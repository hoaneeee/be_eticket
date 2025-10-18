package com.example.E_Ticket.controller.web;

import com.example.E_Ticket.dto.EventDto;
import com.example.E_Ticket.entity.Event;
import com.example.E_Ticket.mapper.EventMapper;
import com.example.E_Ticket.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
@RequiredArgsConstructor
public class HomeWebController {
    private final EventRepository eventRepository;

    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "0") int page,
                       @RequestParam(defaultValue = "12") int size,
                       Model model) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("eventDate").ascending());
        var p = eventRepository.findByStatusAndEventDateGreaterThanEqualOrderByEventDateAsc(
                Event.Status.PUBLISHED, Instant.now(), pageable);

        Page<EventDto> events = new PageImpl<>(
                p.getContent().stream().map(EventMapper::toDto).collect(Collectors.toList()),
                pageable, p.getTotalElements());

        model.addAttribute("events", events);

        //  Tao list số trang de lap o view (0..totalPages-1)
        List<Integer> pages = IntStream.range(0, p.getTotalPages()).boxed().toList();
        model.addAttribute("pages", pages);

        return "index";
    }
}