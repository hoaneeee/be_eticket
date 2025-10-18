package com.example.E_Ticket.repository;

import com.example.E_Ticket.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findByOrder_Id(Long orderId);
    Optional<Ticket> findByCode(String code);
}
