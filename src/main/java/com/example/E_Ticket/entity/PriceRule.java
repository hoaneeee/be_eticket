// src/main/java/com/example/E_Ticket/entity/PriceRule.java
package com.example.E_Ticket.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity @Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Table(name="price_rules", indexes = { @Index(name="idx_rule_event", columnList="event_id") })
public class PriceRule {

    public enum Kind { EARLY_BIRD, FLASH }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="event_id")
    private Event event;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private Kind kind;

    // Chỉ dùng một trong hai — ưu tiên percentOff nếu > 0
    @Column(precision=5, scale=2)
    private BigDecimal percentOff;   // 10.00 = 10%

    @Column(precision=12, scale=2)
    private BigDecimal amountOff;    // VND

    private Instant startsAt;
    private Instant endsAt;

    @Builder.Default
    private Boolean active = true;

    @Column(length=120)
    private String label;            // "EARLY BIRD -10%"
}
