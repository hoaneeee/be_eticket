package com.example.E_Ticket.mapper;

import com.example.E_Ticket.dto.VenueDto;
import com.example.E_Ticket.dto.VenueUpsertReq;
import com.example.E_Ticket.entity.Venue;

public class VenueMapper {
    public static VenueDto toDto(Venue v){
        return new VenueDto(v.getId(), v.getName(), v.getAddress(),
                v.getCapacity(), v.getDescription(), v.getImageUrl());
    }
    public static Venue fromReq(VenueUpsertReq r){
        return Venue.builder()
                .name(r.name()).address(r.address())
                .capacity(r.capacity()).description(r.description())
                .imageUrl(r.imageUrl())
                .build();
    }
    public static void patch(Venue v, VenueUpsertReq r){
        v.setName(r.name());
        v.setAddress(r.address());
        v.setCapacity(r.capacity());
        v.setDescription(r.description());
        v.setImageUrl(r.imageUrl());
    }
}