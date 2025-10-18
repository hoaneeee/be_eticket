package com.example.E_Ticket.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "orders", indexes = {
        @Index(name = "uk_orders_code", columnList = "code", unique = true),
        @Index(name = "idx_orders_event", columnList = "event_id"),
        @Index(name = "idx_orders_user", columnList = "user_id")
})
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 20, unique = true)
    private String code; // ví dụ NX-2025AB12

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id") // có thể null nếu checkout guest
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.PENDING;

    /** COD|BANK|MOMO (mock) */
    @Column(length = 20)
    private String paymentMethod;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;


    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();
    /* total refunded */
    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    @Column(length = 256)
    private String qrImagePath;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Ticket> tickets = new ArrayList<>();

    public enum Status { PENDING, PAID, CANCELLED, CHECKED_IN }

    /* so tien con lai khi refunded */
    public BigDecimal netPaid() {
        return (total == null ? BigDecimal.ZERO : total)
                .subtract(refundedAmount == null ? BigDecimal.ZERO : refundedAmount);
    }
}
