package com.example.E_Ticket.controller.api;

import com.example.E_Ticket.dto.*;
import com.example.E_Ticket.entity.Event;
import com.example.E_Ticket.entity.SeatMap;
import com.example.E_Ticket.entity.Venue;
import com.example.E_Ticket.exception.NotFoundException;
import com.example.E_Ticket.mapper.EventMapper;
import com.example.E_Ticket.repository.EventRepository;
import com.example.E_Ticket.repository.SeatMapRepository;
import com.example.E_Ticket.repository.SeatZoneRepository;
import com.example.E_Ticket.repository.VenueRepository;
import com.example.E_Ticket.service.ZonePriceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/v1/events")
@RequiredArgsConstructor
public class EventAdminApi {
    private final EventRepository eventRepository;
    private final VenueRepository venueRepository;
    private final SeatMapRepository seatMapRepository;
    private final SeatZoneRepository seatZoneRepository;
    private final ZonePriceService zonePriceService;

    @GetMapping
    public Page<EventDto> list(@RequestParam(defaultValue="0") int page,
                               @RequestParam(defaultValue="10") int size,
                               @RequestParam(required=false) String q,
                               @RequestParam(required=false) Long venueId) {
        PageRequest pr = PageRequest.of(page,size, Sort.by("id").descending());
        Page<Event> p =
                    (venueId != null) ? eventRepository.findAllByVenue_Id(venueId, pr) :
                        (q != null && !q.isBlank()) ? eventRepository.findAllByTitleContainingIgnoreCase(q, pr) :
                                eventRepository.findAll(pr);
        return p.map(EventMapper::toDto);
    }
    @GetMapping("/{id}")
    public EventDto getEvent(@PathVariable Long id) throws NotFoundException {
        Event event = eventRepository.findById(id).orElseThrow(
                () -> new NotFoundException("Event not found")
        );
        return EventMapper.toDto(event);
    }

    @GetMapping("/{id}/zones")
    public List<SeatZoneDto> listZonesOfEvent(@PathVariable Long id) {
        Event e = eventRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Event not found"));
        if (e.getSeatMap() == null) {
            throw new NotFoundException("Event chưa gắn SeatMap");
        }
        return seatZoneRepository.findBySeatMap_Id(e.getSeatMap().getId())
                .stream()
                .map(z -> new SeatZoneDto(
                        z.getId(),
                        z.getSeatMap().getId(),
                        z.getCode(),
                        z.getName(),
                        z.getCapacity(),
                        z.getPolygon()
                ))
                .toList();
    }
    @GetMapping("/{id}/zone-prices")
    public List<ZonePriceDto> listZonePricesByEvent(@PathVariable Long id) {
        return zonePriceService.listByEvent(id);
    }
    @PostMapping("/{id}/zone-pricing")
    @ResponseStatus(HttpStatus.CREATED)
    public ZonePriceDto upsertZonePricing(@PathVariable Long id, @RequestBody ZonePriceUpsertReq req) {
        ZonePriceUpsertReq fixed = (req.eventId() == null || !req.eventId().equals(id))
                ? new ZonePriceUpsertReq(id, req.ticketTypeId(), req.seatZoneId(), req.price())
                : req;
        return zonePriceService.upsert(fixed);
    }
    @PostMapping
    public EventDto create(@RequestBody @Valid EventUpsertReq r){
        Venue v = (r.venueId()!=null) ? venueRepository.findById(r.venueId()).orElse(null) : null;
        SeatMap m = (r.seatMapId()!=null) ? seatMapRepository.findById(r.seatMapId()).orElse(null) : null;
        Event e = EventMapper.toEntity(r, v, m);
        return EventMapper.toDto(eventRepository.save(e));
    }

    @PutMapping("/{id}")
    public EventDto update(@PathVariable Long id, @RequestBody @Valid EventUpsertReq r){
        Event e = eventRepository.findById(id).orElseThrow(() -> new NotFoundException("Event not found"));
        Venue v = (r.venueId()!=null) ? venueRepository.findById(r.venueId()).orElse(null) : null;
        SeatMap m = (r.seatMapId()!=null) ? seatMapRepository.findById(r.seatMapId()).orElse(null) : null;
        EventMapper.copyToExisting(e, r, v, m);
        return EventMapper.toDto(eventRepository.save(e));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id){ eventRepository.deleteById(id); }
}
