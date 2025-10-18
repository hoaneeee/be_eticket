package com.example.E_Ticket.repository;

import com.example.E_Ticket.entity.InventoryConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
public interface InventoryConfigRepository extends JpaRepository<InventoryConfig, Long> {
    Optional<InventoryConfig> findByEvent_Id(Long eventId);
}