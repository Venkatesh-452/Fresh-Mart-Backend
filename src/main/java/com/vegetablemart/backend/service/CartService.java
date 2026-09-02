package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.cart.AddToCartRequest;
import com.vegetablemart.backend.dto.cart.CartResponse;
import com.vegetablemart.backend.dto.cart.UpdateCartItemRequest;

public interface CartService {


// =========================================================
// ADD ITEM TO LOGGED-IN USER CART
// =========================================================

    CartResponse addToCart(
            String email,
            AddToCartRequest request
    );


// =========================================================
// GET LOGGED-IN USER CART
// =========================================================

    CartResponse getCart(
            String email
    );


// =========================================================
// UPDATE CART ITEM
// =========================================================

    CartResponse updateCartItem(
            String email,
            Long cartItemId,
            UpdateCartItemRequest request
    );


// =========================================================
// REMOVE CART ITEM
// =========================================================

    void removeCartItem(
            String email,
            Long cartItemId
    );


// =========================================================
// CLEAR LOGGED-IN USER CART
// =========================================================

    void clearCart(
            String email
    );


}
