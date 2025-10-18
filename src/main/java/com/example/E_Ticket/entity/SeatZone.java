package com.example.E_Ticket.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "seat_zones",
        uniqueConstraints = @UniqueConstraint(name = "uk_zone_code_per_map", columnNames = {"seat_map_id","code"})
)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class SeatZone {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Zone thuộc một SeatMap */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seat_map_id", nullable = false)
    private SeatMap seatMap;

    /** Mã ngắn của zone (A1, VIP, STANDARD...) */
    @Column(nullable = false, length = 50)
    private String code;

    /** Tên hiển thị của zone */
    @Column(nullable = false, length = 120)
    private String name;

    /** Sức chứa zone (tuỳ chọn) */
    private Integer capacity;

    /** Tọa độ polygon (string): "x1,y1 x2,y2 x3,y3 ..." — bản đơn giản */
    @Lob
    private String polygon;
}