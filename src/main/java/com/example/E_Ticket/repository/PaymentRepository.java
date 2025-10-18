package com.example.E_Ticket.repository;

import com.example.E_Ticket.entity.Payment;
import jakarta.validation.constraints.Email;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findTopByOrderIdOrderByIdDesc(Long orderId);

    Optional<Payment> findByMomoOrderId(String momoOrderId);
}
