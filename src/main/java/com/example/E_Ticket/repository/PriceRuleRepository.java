// src/main/java/com/example/E_Ticket/repository/PriceRuleRepository.java
package com.example.E_Ticket.repository;

import com.example.E_Ticket.entity.PriceRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface PriceRuleRepository extends JpaRepository<PriceRule, Long> {

    @Query("""
        select r from PriceRule r
        where r.event.id = :eventId
          and r.active = true
          and (r.startsAt is null or r.startsAt <= :now)
          and (r.endsAt   is null or r.endsAt   >= :now)
    """)
    List<PriceRule> findActiveByEventAt(Long eventId, Instant now);
}
