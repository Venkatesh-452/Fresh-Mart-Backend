package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.cart.AddToCartRequest;
import com.vegetablemart.backend.dto.cart.CartResponse;
import com.vegetablemart.backend.dto.cart.UpdateCartItemRequest;

public interface CartService {

    CartResponse addToCart(Long userId, AddToCartRequest request);

    CartResponse getCart(Long userId);

    CartResponse updateCartItem(
            Long userId,
            Long cartItemId,
            UpdateCartItemRequest request
    );

    void removeCartItem(Long userId, Long cartItemId);

    void clearCart(Long userId);
}