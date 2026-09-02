package com.vegetablemart.backend.dto.order;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlaceOrderRequest {

    @NotNull(message = "Address ID is required")
    @Positive(message = "Address ID must be positive")
    private Long addressId;
}
