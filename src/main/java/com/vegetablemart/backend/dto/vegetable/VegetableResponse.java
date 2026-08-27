package com.vegetablemart.backend.dto.vegetable;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
public class VegetableResponse {

    private Long id;

    private String name;

    private String description;

    private BigDecimal price;

    private BigDecimal quantity;

    private String unit;

    private String imageUrl;

    private Boolean active;

    private Long categoryId;

    private String categoryName;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}