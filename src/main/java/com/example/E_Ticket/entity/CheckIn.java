package com.example.E_Ticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "checkins",
        indexes = {
                @Index(name = "uk_checkins_code", columnList = "code", unique = true),
                @Index(name = "idx_checkins_order", columnList = "order_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckIn {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Mã QR/Hash duy nhất cho lượt vào cửa */
    @Column(nullable = false, length = 64, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /** ID nhân viên quét */
    private Long scannedBy;

    private Instant scannedAt;
}
