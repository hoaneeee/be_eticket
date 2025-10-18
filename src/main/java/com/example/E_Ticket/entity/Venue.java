package com.example.E_Ticket.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name="venues")
public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, length=120)
    private String name;
    @Column(nullable=false, length=255)
    private String address;

    private Integer capacity;
    @Column(length=500) private String description;
    private String imageUrl;
}
