package com.example.E_Ticket.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(
        name = "sold_seat",
        uniqueConstraints = @UniqueConstraint(name="uk_soldseat_ev_zone_no", columnNames = {"event_id","zone_id","seat_no"}),
        indexes = {
                @Index(name="ix_soldseat_event", columnList = "event_id"),
                @Index(name="ix_soldseat_zone", columnList = "zone_id")
        }
)
public class SoldSeat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="event_id", nullable=false)
    private Event event;

    @ManyToOne(optional=false) @JoinColumn(name="zone_id", nullable=false)
    private SeatZone zone;

    @Column(name="seat_no", nullable=false)
    private Integer seatNo;

    /** để truy vết */
    @ManyToOne(optional=false) @JoinColumn(name="order_id", nullable=false)
    private Order order;

    @CreationTimestamp
    @Column(nullable=false, updatable=false)
    private Instant createdAt;
}
