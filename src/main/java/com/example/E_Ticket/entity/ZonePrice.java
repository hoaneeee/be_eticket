package com.example.E_Ticket.entity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(
        name = "zone_prices",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_zone_price_unique",
                columnNames = {"event_id","ticket_type_id","seat_zone_id"}
        )
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ZonePrice {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Event áp dụng giá */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /** Loại vé của event */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_type_id", nullable = false)
    private TicketType ticketType;

    /** Khu áp dụng */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_zone_id", nullable = false)
    private SeatZone seatZone;

    /** Giá ghi đè theo zone (VND) */
    @Column(nullable = false)
    private BigDecimal price;
}