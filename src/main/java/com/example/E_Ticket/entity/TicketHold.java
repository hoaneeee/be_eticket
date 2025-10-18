package com.example.E_Ticket.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name="ticket_hold", indexes = {
        @Index(name="ix_hold_expires", columnList = "expires_at"),
        @Index(name="ix_hold_event_type", columnList = "event_id,ticket_type_id")
})
public class TicketHold {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false) @JoinColumn(name="event_id")
    private Event event;

    @ManyToOne(optional = false) @JoinColumn(name="ticket_type_id")
    private TicketType ticketType;

    @Column(nullable = false)
    private Integer qty;

    // dùng để ràng buộc một người/một session , để release đúng
    private Long userId;
    private String sessionId;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name="expires_at", nullable = false)
    private Instant expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Builder.Default
    @Column(nullable = false)
    private Integer renewCount = 0;

    public enum Status { ACTIVE, RELEASED, CONSUMED, EXPIRED }
}