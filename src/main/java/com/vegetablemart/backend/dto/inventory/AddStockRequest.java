package com.vegetablemart.backend.dto.inventory;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddStockRequest {

    @NotNull(message = "Vegetable ID is required")
    private Long vegetableId;

    @NotNull(message = "Stock quantity is required")
    @DecimalMin(
            value = "0.01",
            message = "Stock quantity must be greater than 0"
    )
    private BigDecimal quantity;
}