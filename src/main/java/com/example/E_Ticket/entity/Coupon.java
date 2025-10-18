package com.example.E_Ticket.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;

@Entity
@Table(name="coupons", indexes = {
        @Index(name="uk_coupon_code", columnList="code", unique=true)
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Coupon {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique=true, length=80)
    private String code;                     // eg: EARLY10

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    private Type type;                       // PERCENT | AMOUNT

    @Column(nullable=false)
    private Long value;                    //% or so tien vnd

    private Instant startAt;
    private Instant endAt;

    private Integer maxUse;
    private Integer perUserLimit;

    @Builder.Default
    @Column(nullable=false)
    private Integer used = 0;

    public enum Type { PERCENT, AMOUNT }
}
