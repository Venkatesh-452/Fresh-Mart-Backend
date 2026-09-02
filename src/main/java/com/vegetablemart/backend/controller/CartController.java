package com.vegetablemart.backend.controller;

import com.vegetablemart.backend.dto.cart.AddToCartRequest;
import com.vegetablemart.backend.dto.cart.CartResponse;
import com.vegetablemart.backend.dto.cart.UpdateCartItemRequest;
import com.vegetablemart.backend.service.CartService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {


    private final CartService cartService;


// =========================================================
// ADD ITEM TO CART
// =========================================================

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(

            Authentication authentication,

            @Valid @RequestBody AddToCartRequest request
    ) {

        String email =
                authentication.getName();

        return ResponseEntity.ok(

                cartService.addToCart(
                        email,
                        request
                )
        );
    }


// =========================================================
// GET LOGGED-IN USER CART
// =========================================================

    @GetMapping
    public ResponseEntity<CartResponse> getCart(

            Authentication authentication
    ) {

        String email =
                authentication.getName();

        return ResponseEntity.ok(

                cartService.getCart(email)
        );
    }


// =========================================================
// UPDATE CART ITEM
// =========================================================

    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItem(

            Authentication authentication,

            @PathVariable Long cartItemId,

            @Valid @RequestBody UpdateCartItemRequest request
    ) {

        String email =
                authentication.getName();

        return ResponseEntity.ok(

                cartService.updateCartItem(

                        email,

                        cartItemId,

                        request
                )
        );
    }


// =========================================================
// REMOVE CART ITEM
// =========================================================

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<String> removeCartItem(

            Authentication authentication,

            @PathVariable Long cartItemId
    ) {

        String email =
                authentication.getName();

        cartService.removeCartItem(
                email,
                cartItemId
        );

        return ResponseEntity.ok(
                "Cart item removed successfully"
        );
    }


// =========================================================
// CLEAR CART
// =========================================================

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(

            Authentication authentication
    ) {

        String email =
                authentication.getName();

        cartService.clearCart(email);

        return ResponseEntity.ok(
                "Cart cleared successfully"
        );
    }


}
