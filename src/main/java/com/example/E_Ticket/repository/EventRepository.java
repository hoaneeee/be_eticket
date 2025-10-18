package com.example.E_Ticket.repository;

import com.example.E_Ticket.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findBySlug(String id);

    boolean existsBySlug(String slug);
    boolean existsBySlugAndIdNot(String slug, Long id);

    @Query("""
        SELECT e FROM Event e
        WHERE (:q IS NULL OR :q = '' OR
               LOWER(e.title) LIKE LOWER(CONCAT('%', :q, '%')) )
        """)
    Page<Event> search(String q, Pageable pageable);

    long countByStatus (Event.Status status);
    long countByEventDateAfter(Instant instant);
    Page<Event> findAllByVenue_Id(Long venueId, Pageable p);
    Page<Event> findAllByTitleContainingIgnoreCase(String q, Pageable p);
    boolean existsByVenue_Id(Long venueId);

    // index
    Page<Event> findByStatusAndEventDateGreaterThanEqualOrderByEventDateAsc(
            Event.Status status, Instant from, Pageable pageable);    // chan xoa cac venue using

}
