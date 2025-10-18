package com.example.E_Ticket.repository;

import com.example.E_Ticket.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository

public interface RefundRepository extends JpaRepository<Refund, Long> {
    List<Refund> findByOrder_Id(Long orderId);
    long countByStatus(Refund.Status status);
    long countByOrder_User_IdAndUpdatedAtAfter(Long userId, Instant since);
    List<Refund> findTop10ByOrder_User_IdAndUpdatedAtAfterOrderByUpdatedAtDesc(Long userId, Instant since);
    List<Refund> findTop20ByStatusOrderByCreatedAtDesc(Refund.Status status);
    long countByOrder_User_EmailAndUpdatedAtAfter(String email, Instant since);

    List<Refund> findTop10ByOrder_User_EmailAndUpdatedAtAfterOrderByUpdatedAtDesc(String email, Instant since);
}