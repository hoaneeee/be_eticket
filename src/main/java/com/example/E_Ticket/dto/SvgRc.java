package com.example.E_Ticket.dto;

import com.example.E_Ticket.entity.Event;
import com.example.E_Ticket.entity.SeatMap;
import com.example.E_Ticket.entity.SeatZone;

import java.util.List;

public record SvgRc(
        Event event,
        SeatMap map,
        List<SeatZone> zones,
        String inlineSvg) {
}
