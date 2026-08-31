package com.vegetablemart.backend.dto.inventory;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {

    private Long inventoryId;

    private Long vegetableId;

    private String vegetableName;

    private String imageUrl;

    private BigDecimal totalStock;

    private BigDecimal soldQuantity;

    private BigDecimal availableQuantity;

    private String unit;

    private LocalDateTime lastUpdated;
}