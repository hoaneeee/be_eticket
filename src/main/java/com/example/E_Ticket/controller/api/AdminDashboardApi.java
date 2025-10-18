package com.example.E_Ticket.controller.api;

import com.example.E_Ticket.entity.Event;
import com.example.E_Ticket.entity.Order;
import com.example.E_Ticket.repository.EventRepository;
import com.example.E_Ticket.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/v1/dashboard")
public class AdminDashboardApi {

    private final OrderRepository orderRepository;
    private final EventRepository eventRepository;

    @GetMapping("/summary")
    public Map<String, Object> summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        Instant start = (from != null
                ? from.atStartOfDay(ZoneId.systemDefault()).toInstant()
                : Instant.now().minus(14, ChronoUnit.DAYS));
        Instant end = (to != null
                ? to.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant()
                : Instant.now());

        // ĐÚNG: truyền Enum thay vì String
        var paid = orderRepository.findByStatusAndCreatedAtBetween(Order.Status.PAID, start, end);

        // totals
        long totalTickets = paid.stream()
                .flatMap(o -> o.getItems().stream())
                .mapToInt(i -> i.getQty())
                .sum();

        long publishedEvents = eventRepository.countByStatus(Event.Status.PUBLISHED);
        long upcomingEvents = eventRepository.countByEventDateAfter(Instant.now());
        long totalPaidOrders = paid.size();

        // use BigDecimal cho doanh thu chinh xac tuyet doi
        BigDecimal totalRevenue = paid.stream()
                .map(o -> o.getTotal())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // revenueByDate (use BigDecimal)
        Map<String, BigDecimal> revenueByDate = new TreeMap<>();
        paid.forEach(o -> {
            String d = o.getCreatedAt().atZone(ZoneId.systemDefault()).toLocalDate().toString();
            revenueByDate.merge(d, o.getTotal(), BigDecimal::add);
        });

        // ---- Top events (group theo Event) ----
        // i.getEvent() 0 -> di qua TicketType -> Event
        Map<Long, BigDecimal> revenueByEvent = new HashMap<>();
        Map<Long, Long> ticketsByEvent = new HashMap<>();

        paid.forEach(o -> o.getItems().forEach(i -> {
            Long eventId = i.getTicketType().getEvent().getId();
            BigDecimal itemRevenue = i.getPrice().multiply(BigDecimal.valueOf(i.getQty()));
            revenueByEvent.merge(eventId, itemRevenue, BigDecimal::add);
            ticketsByEvent.merge(eventId, (long) i.getQty(), Long::sum);
        }));

        // preload title 1 lần để tránh N+1
        var eventTitles = eventRepository.findAllById(revenueByEvent.keySet()).stream()
                .collect(Collectors.toMap(Event::getId, Event::getTitle));

        List<Map<String, Object>> topEvents = revenueByEvent.entrySet().stream()
                .sorted((a, b) -> b.getValue().compareTo(a.getValue()))
                .limit(5)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("eventId", e.getKey());
                    m.put("title", eventTitles.getOrDefault(e.getKey(), "Event " + e.getKey()));
                    m.put("revenue", e.getValue());
                    m.put("tickets", ticketsByEvent.getOrDefault(e.getKey(), 0L));
                    return m;
                })
                .collect(Collectors.toList());

        return Map.of(
                "summary", Map.of(
                        "totalRevenue", totalRevenue,
                        "totalPaidOrders", totalPaidOrders,
                        "totalTicketsSold", totalTickets,
                        "publishedEvents", publishedEvents,
                        "upcomingEvents", upcomingEvents
                ),
                "revenueByDate", revenueByDate.entrySet().stream()
                        .map(e -> Map.of("date", e.getKey(), "amount", e.getValue()))
                        .toList(),
                "topEvents", topEvents
        );
    }
}
