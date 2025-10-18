package com.example.E_Ticket.repository;

import com.example.E_Ticket.entity.TicketHold;
import com.example.E_Ticket.entity.TicketHold.Status;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
@Repository
public interface TicketHoldRepository extends JpaRepository<TicketHold, Long> {
    List<TicketHold> findByTicketType_IdAndStatusAndExpiresAtAfter(Long ticketTypeId, Status status, Instant now);
    List<TicketHold> findByStatusAndExpiresAtBefore(Status status, Instant time);
    Optional<TicketHold> findByIdAndStatus(Long id, Status status);
    List<TicketHold> findByEvent_IdAndSessionIdAndStatus(Long eventId, String sessionId, Status status);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           select h
           from TicketHold h
           where h.id = :id and h.status = :active
           """)
    Optional<TicketHold> lockActiveById(@Param("id") Long id,
                                        @Param("active") Status active);

    @Modifying
    @Query("""
           delete from TicketHold h
           where h.status = :expired or h.expiresAt < :now
           """)
    int purgeExpired(@Param("expired") Status expired,
                     @Param("now") Instant now);
}