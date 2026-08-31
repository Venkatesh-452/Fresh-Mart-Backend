package com.vegetablemart.backend.dto.payment;

import com.vegetablemart.backend.entity.PaymentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusRequest {

    @NotNull(message = "Payment status is required")
    private PaymentStatus status;

    private String transactionId;
}