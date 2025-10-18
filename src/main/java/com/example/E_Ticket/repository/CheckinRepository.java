package com.example.E_Ticket.repository;

import com.example.E_Ticket.entity.CheckIn;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CheckinRepository extends JpaRepository<CheckIn, Long> {
    Optional<CheckIn> findByCode(String code );
}
