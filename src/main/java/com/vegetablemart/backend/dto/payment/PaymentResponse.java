package com.vegetablemart.backend.dto.payment;

import com.vegetablemart.backend.entity.PaymentMethod;
import com.vegetablemart.backend.entity.PaymentStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponse {

    private Long paymentId;

    private Long orderId;

    private Long userId;

    private String customerName;

    private BigDecimal amount;

    private PaymentMethod paymentMethod;

    private PaymentStatus status;

    private String transactionId;

    private LocalDateTime paymentDate;

    private LocalDateTime createdAt;
}