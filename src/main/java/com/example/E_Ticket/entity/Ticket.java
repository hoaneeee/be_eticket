package com.example.E_Ticket.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name="tickets", indexes = {
        @Index(name="idx_tickets_order", columnList="order_id"),
        @Index(name="uk_tickets_code", columnList="code", unique = true)
})
public class Ticket {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="order_id")
    private Order order;

    @Column(nullable = false, length = 32)
    private String code;

    @Column(nullable = false, length = 128)
    private String qrContent;           // payload để quét

    @Column(length = 256)
    private String qrImagePath;         // file:uploads/qr/xxx.png

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status;              // NEW | CHECKED_IN | VOID

    @CreationTimestamp
    private Instant createdAt;
    @Column(nullable = false)
    private int quantity; // Quantity of tickets

    @Column(nullable = false)
    private long price; // Price of the ticket


    @ManyToOne(fetch = FetchType.LAZY) // Assuming you have a TicketType entity
    @JoinColumn(name = "ticket_type_id")
    private TicketType ticketType; // New field for ticket type

    public enum Status { NEW, CHECKED_IN, VOID }
}
