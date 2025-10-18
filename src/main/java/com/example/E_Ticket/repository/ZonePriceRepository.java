package com.example.E_Ticket.repository;

import com.example.E_Ticket.entity.ZonePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZonePriceRepository extends JpaRepository<ZonePrice, Long> {
    List<ZonePrice> findByEvent_Id(Long eventId);
    Optional<ZonePrice> findByEvent_IdAndTicketType_IdAndSeatZone_Id(Long eventId, Long ticketTypeId, Long seatZoneId);
    Optional<ZonePrice> findFirstByEvent_IdAndSeatZone_Id(Long eventId, Long seatZoneId);

}

