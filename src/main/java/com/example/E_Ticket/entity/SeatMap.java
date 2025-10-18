package com.example.E_Ticket.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(name = "seat_maps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatMap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id", nullable = false)
    private Venue venue;


    @Column(nullable = false, length = 120)
    private String name;

    /** path svg eg: "/uploads/hall-a-xxx.svg" */
    @Column(nullable = false, length = 255)
    private String svgPath;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}