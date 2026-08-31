package com.vegetablemart.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "vegetable_id",
            nullable = false,
            unique = true
    )
    private Vegetable vegetable;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalStock;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal soldQuantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal availableQuantity;

    @Column(nullable = false)
    private LocalDateTime lastUpdated;

    @PrePersist
    protected void onCreate() {
        if (soldQuantity == null) {
            soldQuantity = BigDecimal.ZERO;
        }

        if (availableQuantity == null) {
            availableQuantity = totalStock;
        }

        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}