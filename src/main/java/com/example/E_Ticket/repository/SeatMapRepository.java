package com.example.E_Ticket.repository;

import com.example.E_Ticket.entity.SeatMap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeatMapRepository extends JpaRepository<SeatMap, Long> {
    List<SeatMap> findByVenue_IdOrderByIdDesc(Long venueId);
    Optional<SeatMap> findById(Long id);
}