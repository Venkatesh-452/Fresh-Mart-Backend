package com.vegetablemart.backend.controller;

import com.vegetablemart.backend.dto.cart.AddToCartRequest;
import com.vegetablemart.backend.dto.cart.CartResponse;
import com.vegetablemart.backend.dto.cart.UpdateCartItemRequest;
import com.vegetablemart.backend.service.CartService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @PostMapping("/add")
    public ResponseEntity<CartResponse> addToCart(
            @RequestParam Long userId,
            @Valid @RequestBody AddToCartRequest request
    ) {

        return ResponseEntity.ok(
                cartService.addToCart(userId, request)
        );
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart(
            @RequestParam Long userId
    ) {

        return ResponseEntity.ok(
                cartService.getCart(userId)
        );
    }

    @PutMapping("/item/{cartItemId}")
    public ResponseEntity<CartResponse> updateCartItem(
            @RequestParam Long userId,
            @PathVariable Long cartItemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {

        return ResponseEntity.ok(
                cartService.updateCartItem(
                        userId,
                        cartItemId,
                        request
                )
        );
    }

    @DeleteMapping("/item/{cartItemId}")
    public ResponseEntity<String> removeCartItem(
            @RequestParam Long userId,
            @PathVariable Long cartItemId
    ) {

        cartService.removeCartItem(
                userId,
                cartItemId
        );

        return ResponseEntity.ok(
                "Cart item removed successfully"
        );
    }

    @DeleteMapping("/clear")
    public ResponseEntity<String> clearCart(
            @RequestParam Long userId
    ) {

        cartService.clearCart(userId);

        return ResponseEntity.ok(
                "Cart cleared successfully"
        );
    }
}