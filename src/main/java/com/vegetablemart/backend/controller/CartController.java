package com.vegetablemart.backend.controller;

import com.vegetablemart.backend.dto.cart.AddToCartRequest;
import com.vegetablemart.backend.dto.cart.CartResponse;
import com.vegetablemart.backend.dto.cart.UpdateCartItemRequest;
import com.vegetablemart.backend.service.CartService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Validated
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(
            Authentication authentication,
            @Valid @RequestBody AddToCartRequest request) {

        return ResponseEntity.ok(
                cartService.addToCart(authentication.getName(), request)
        );
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            Authentication authentication) {

        return ResponseEntity.ok(
                cartService.getCart(authentication.getName())
        );
    }

    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            Authentication authentication,
            @PathVariable @Positive(message = "Cart item ID must be positive") Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        return ResponseEntity.ok(
                cartService.updateCartItem(
                        authentication.getName(),
                        cartItemId,
                        request
                )
        );
    }

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<Void> removeCartItem(
            Authentication authentication,
            @PathVariable @Positive(message = "Cart item ID must be positive") Long cartItemId) {

        cartService.removeCartItem(authentication.getName(), cartItemId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear")
    public ResponseEntity<Void> clearCart(
            Authentication authentication) {

        cartService.clearCart(authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
