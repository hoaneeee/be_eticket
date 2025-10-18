package com.example.E_Ticket.repository;

import com.example.E_Ticket.entity.SeatZone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatZoneRepository extends JpaRepository<SeatZone, Long> {
    List<SeatZone> findBySeatMap_IdOrderByIdAsc(Long seatMapId);
    List<SeatZone> findBySeatMap_Id(Long seatMapId);

    Optional<SeatZone> findBySeatMap_IdAndCode(Long mapId, String code);
}
