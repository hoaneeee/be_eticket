package com.example.E_Ticket.mapper;

import com.example.E_Ticket.dto.EventDto;
import com.example.E_Ticket.dto.EventUpsertReq;
import com.example.E_Ticket.entity.Event;
import com.example.E_Ticket.entity.SeatMap;
import com.example.E_Ticket.entity.Venue;

public class EventMapper {

    public static EventDto toDto(Event e){
        return new EventDto(
                e.getId(),
                e.getTitle(),
                e.getSlug(),
                e.getEventDate(),
                e.getStatus().name(),
                e.getBannerUrl(),
                e.getVenue()!=null? e.getVenue().getId() : null,
                e.getVenue()!=null? e.getVenue().getName() : null,
                e.getVenue()!=null? e.getVenue().getAddress() : null,
                e.getSeatMap()!=null? e.getSeatMap().getId() : null,
                e.getDescription()
        );
    }

    public static Event toEntity(EventUpsertReq r, Venue v, SeatMap m){
        Event e = new Event();
        e.setTitle(r.title());
        e.setSlug(r.slug());
        e.setEventDate(r.eventDate());
        e.setStatus(Event.Status.valueOf(r.status()));
        e.setVenue(v);
        e.setSeatMap(m);                    // thêm
        e.setBannerUrl(r.bannerUrl());
        e.setDescription(r.description());
        return e;
    }

    public static void copyToExisting(Event e, EventUpsertReq r, Venue v, SeatMap m){
        e.setTitle(r.title());
        e.setSlug(r.slug());
        e.setEventDate(r.eventDate());
        e.setStatus(Event.Status.valueOf(r.status()));
        e.setVenue(v);
        e.setSeatMap(m);                    // thêm
        e.setBannerUrl(r.bannerUrl());
        e.setDescription(r.description());
    }
}
