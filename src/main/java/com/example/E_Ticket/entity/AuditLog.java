package com.example.E_Ticket.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name = "audit_logs", indexes = {
        @Index(name="idx_audit_actor", columnList = "actor"),
        @Index(name="idx_audit_entity", columnList = "entityType,entityId")
})
public class AuditLog {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*Nguoi thao tac (email admin) */
    @Column(nullable = false, length = 120)
    private String actor;

    /* ACTION: REFUND_CREATE, REFUND_APPROVE, REFUND_PAY, ... */
    @Column(nullable = false, length = 60)
    private String action;

    /* kieu doi tuong va id de truy xuat nguoc */
    @Column(nullable = false, length = 60)
    private String entityType;

    @Column(nullable = false)
    private Long entityId;

    /* Thong tin them */
    @Column(length = 1000)
    private String details;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
