package com.vegetablemart.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime orderDate;

    // Snapshot of the address used at checkout.
    // This keeps historical orders unchanged if the customer later edits/deletes the address.
    @Column(nullable = false, length = 100)
    private String deliveryFullName;

    @Column(nullable = false, length = 15)
    private String deliveryPhone;

    @Column(nullable = false, length = 255)
    private String deliveryAddressLine;

    @Column(nullable = false, length = 100)
    private String deliveryCity;

    @Column(nullable = false, length = 100)
    private String deliveryState;

    @Column(nullable = false, length = 10)
    private String deliveryPincode;

    @Column(length = 150)
    private String deliveryLandmark;

    @OneToMany(
            mappedBy = "order",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();

        if (status == null) {
            status = OrderStatus.PLACED;
        }
    }
}
