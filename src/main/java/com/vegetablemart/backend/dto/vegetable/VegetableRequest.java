package com.vegetablemart.backend.dto.vegetable;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class VegetableRequest {

    @NotBlank(message = "Vegetable name is required")
    @Size(
            min = 2,
            max = 100,
            message = "Vegetable name must be between 2 and 100 characters"
    )
    private String name;

    @Size(
            max = 500,
            message = "Description cannot exceed 500 characters"
    )
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(
            value = "0.01",
            message = "Price must be greater than 0"
    )
    private BigDecimal price;

    @NotNull(message = "Quantity is required")
    @DecimalMin(
            value = "0.01",
            message = "Quantity must be greater than 0"
    )
    private BigDecimal quantity;

    @NotBlank(message = "Unit is required")
    @Size(
            max = 20,
            message = "Unit cannot exceed 20 characters"
    )
    private String unit;

    @Size(
            max = 500,
            message = "Image URL cannot exceed 500 characters"
    )
    private String imageUrl;

    @NotNull(message = "Category ID is required")
    @Positive(message = "Category ID must be greater than 0")
    private Long categoryId;
}