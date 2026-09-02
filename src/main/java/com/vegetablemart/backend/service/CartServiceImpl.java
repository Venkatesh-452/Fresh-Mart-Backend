package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.cart.AddToCartRequest;
import com.vegetablemart.backend.dto.cart.CartItemResponse;
import com.vegetablemart.backend.dto.cart.CartResponse;
import com.vegetablemart.backend.dto.cart.UpdateCartItemRequest;

import com.vegetablemart.backend.entity.Cart;
import com.vegetablemart.backend.entity.CartItem;
import com.vegetablemart.backend.entity.User;
import com.vegetablemart.backend.entity.Vegetable;

import com.vegetablemart.backend.repository.CartItemRepository;
import com.vegetablemart.backend.repository.CartRepository;
import com.vegetablemart.backend.repository.UserRepository;
import com.vegetablemart.backend.repository.VegetableRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CartServiceImpl implements CartService {


    private final CartRepository cartRepository;

    private final CartItemRepository cartItemRepository;

    private final VegetableRepository vegetableRepository;

    private final UserRepository userRepository;


// =========================================================
// ADD ITEM TO CART
// =========================================================

    @Override
    public CartResponse addToCart(
            String email,
            AddToCartRequest request
    ) {

        User user = getUserByEmail(email);


        Vegetable vegetable =
                vegetableRepository.findById(
                                request.getVegetableId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Vegetable not found with ID: "
                                                + request.getVegetableId()
                                )
                        );


        // Check whether vegetable is active
        if (!Boolean.TRUE.equals(
                vegetable.getActive()
        )) {

            throw new RuntimeException(
                    "Vegetable is not available"
            );
        }


        // Check requested quantity against stock
        if (request.getQuantity()
                .compareTo(vegetable.getQuantity()) > 0) {

            throw new RuntimeException(
                    "Insufficient stock. Available quantity: "
                            + vegetable.getQuantity()
            );
        }


        // Find existing cart or create a new one
        Cart cart =
                cartRepository.findByUserId(
                                user.getId()
                        )
                        .orElseGet(() -> {

                            Cart newCart = Cart.builder()
                                    .user(user)
                                    .build();

                            return cartRepository.save(newCart);
                        });


        // Find existing cart item
        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndVegetableId(
                                cart.getId(),
                                vegetable.getId()
                        )
                        .orElse(null);


        if (cartItem != null) {

            // Existing quantity + requested quantity
            BigDecimal newQuantity =
                    cartItem.getQuantity()
                            .add(request.getQuantity());


            // Make sure total quantity doesn't exceed stock
            if (newQuantity.compareTo(
                    vegetable.getQuantity()
            ) > 0) {

                throw new RuntimeException(
                        "Cannot add more than available stock. "
                                + "Available quantity: "
                                + vegetable.getQuantity()
                );
            }


            cartItem.setQuantity(newQuantity);


            // Update price to current vegetable price
            cartItem.setPrice(
                    vegetable.getPrice()
            );

        } else {

            // Create new cart item
            cartItem = CartItem.builder()
                    .cart(cart)
                    .vegetable(vegetable)
                    .quantity(request.getQuantity())
                    .price(vegetable.getPrice())
                    .build();
        }


        cartItemRepository.save(cartItem);


        return buildCartResponse(cart);
    }


// =========================================================
// GET CART
// =========================================================

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(
            String email
    ) {

        User user = getUserByEmail(email);


        Cart cart =
                cartRepository.findByUserId(
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cart not found"
                                )
                        );


        return buildCartResponse(cart);
    }


// =========================================================
// UPDATE CART ITEM
// =========================================================

    @Override
    public CartResponse updateCartItem(
            String email,
            Long cartItemId,
            UpdateCartItemRequest request
    ) {

        User user = getUserByEmail(email);


        Cart cart =
                cartRepository.findByUserId(
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cart not found"
                                )
                        );


        CartItem cartItem =
                cartItemRepository.findById(
                                cartItemId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cart item not found"
                                )
                        );


        // IMPORTANT:
        // Verify that this cart item belongs
        // to the logged-in user's cart.
        if (!cartItem.getCart()
                .getId()
                .equals(cart.getId())) {

            throw new RuntimeException(
                    "Cart item does not belong to this user"
            );
        }


        Vegetable vegetable =
                cartItem.getVegetable();


        // Check current stock
        if (request.getQuantity()
                .compareTo(
                        vegetable.getQuantity()
                ) > 0) {

            throw new RuntimeException(
                    "Insufficient stock. Available quantity: "
                            + vegetable.getQuantity()
            );
        }


        // Check vegetable availability
        if (!Boolean.TRUE.equals(
                vegetable.getActive()
        )) {

            throw new RuntimeException(
                    "Vegetable is not available"
            );
        }


        cartItem.setQuantity(
                request.getQuantity()
        );


        // Keep price synchronized with current price
        cartItem.setPrice(
                vegetable.getPrice()
        );


        cartItemRepository.save(cartItem);


        return buildCartResponse(cart);
    }


// =========================================================
// REMOVE CART ITEM
// =========================================================

    @Override
    public void removeCartItem(
            String email,
            Long cartItemId
    ) {

        User user = getUserByEmail(email);


        Cart cart =
                cartRepository.findByUserId(
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cart not found"
                                )
                        );


        CartItem cartItem =
                cartItemRepository.findById(
                                cartItemId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cart item not found"
                                )
                        );


        // Prevent deleting another user's cart item
        if (!cartItem.getCart()
                .getId()
                .equals(cart.getId())) {

            throw new RuntimeException(
                    "Cart item does not belong to this user"
            );
        }


        cartItemRepository.delete(cartItem);
    }


// =========================================================
// CLEAR CART
// =========================================================

    @Override
    public void clearCart(
            String email
    ) {

        User user = getUserByEmail(email);


        Cart cart =
                cartRepository.findByUserId(
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Cart not found"
                                )
                        );


        cart.getItems().clear();

        cartRepository.save(cart);
    }


// =========================================================
// FIND USER BY EMAIL
// =========================================================

    private User getUserByEmail(
            String email
    ) {

        return userRepository
                .findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with email: "
                                        + email
                        )
                );
    }


// =========================================================
// BUILD CART RESPONSE
// =========================================================

    private CartResponse buildCartResponse(
            Cart cart
    ) {

        List<CartItemResponse> items =
                cart.getItems()
                        .stream()
                        .map(this::mapToCartItemResponse)
                        .toList();


        BigDecimal totalAmount =
                items.stream()
                        .map(
                                CartItemResponse::getSubtotal
                        )
                        .reduce(
                                BigDecimal.ZERO,
                                BigDecimal::add
                        );


        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }


// =========================================================
// MAP CART ITEM RESPONSE
// =========================================================

    private CartItemResponse mapToCartItemResponse(
            CartItem item
    ) {

        BigDecimal subtotal =
                item.getPrice()
                        .multiply(item.getQuantity());


        return CartItemResponse.builder()
                .id(item.getId())
                .vegetableId(
                        item.getVegetable().getId()
                )
                .vegetableName(
                        item.getVegetable().getName()
                )
                .imageUrl(
                        item.getVegetable().getImageUrl()
                )
                .quantity(
                        item.getQuantity()
                )
                .price(
                        item.getPrice()
                )
                .subtotal(subtotal)
                .build();
    }


}
