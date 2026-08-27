package com.vegetablemart.backend.dto.cart;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {

    private Long id;

    private Long vegetableId;

    private String vegetableName;

    private String imageUrl;

    private BigDecimal quantity;

    private BigDecimal price;

    private BigDecimal subtotal;
}