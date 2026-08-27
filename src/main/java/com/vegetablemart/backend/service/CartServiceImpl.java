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
import com.vegetablemart.backend.service.CartService;
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

    @Override
    public CartResponse addToCart(
            Long userId,
            AddToCartRequest request
    ) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException("User not found with ID: " + userId)
                );

        Vegetable vegetable = vegetableRepository.findById(
                request.getVegetableId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Vegetable not found with ID: "
                                + request.getVegetableId()
                )
        );

        if (!Boolean.TRUE.equals(vegetable.getActive())) {
            throw new RuntimeException("Vegetable is not available");
        }

        if (request.getQuantity().compareTo(vegetable.getQuantity()) > 0) {
            throw new RuntimeException(
                    "Insufficient stock. Available quantity: "
                            + vegetable.getQuantity()
            );
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {

                    Cart newCart = Cart.builder()
                            .user(user)
                            .build();

                    return cartRepository.save(newCart);
                });

        CartItem cartItem = cartItemRepository
                .findByCartIdAndVegetableId(
                        cart.getId(),
                        vegetable.getId()
                )
                .orElse(null);

        if (cartItem != null) {

            BigDecimal newQuantity =
                    cartItem.getQuantity()
                            .add(request.getQuantity());

            if (newQuantity.compareTo(vegetable.getQuantity()) > 0) {
                throw new RuntimeException(
                        "Cannot add more than available stock. "
                                + "Available quantity: "
                                + vegetable.getQuantity()
                );
            }

            cartItem.setQuantity(newQuantity);

        } else {

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

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found")
                );

        return buildCartResponse(cart);
    }

    @Override
    public CartResponse updateCartItem(
            Long userId,
            Long cartItemId,
            UpdateCartItemRequest request
    ) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found")
                );

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new RuntimeException("Cart item not found")
                );

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException(
                    "Cart item does not belong to this user"
            );
        }

        Vegetable vegetable = cartItem.getVegetable();

        if (request.getQuantity().compareTo(vegetable.getQuantity()) > 0) {
            throw new RuntimeException(
                    "Insufficient stock. Available quantity: "
                            + vegetable.getQuantity()
            );
        }

        cartItem.setQuantity(request.getQuantity());

        cartItemRepository.save(cartItem);

        return buildCartResponse(cart);
    }

    @Override
    public void removeCartItem(
            Long userId,
            Long cartItemId
    ) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found")
                );

        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() ->
                        new RuntimeException("Cart item not found")
                );

        if (!cartItem.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException(
                    "Cart item does not belong to this user"
            );
        }

        cartItemRepository.delete(cartItem);
    }

    @Override
    public void clearCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException("Cart not found")
                );

        cart.getItems().clear();

        cartRepository.save(cart);
    }

    private CartResponse buildCartResponse(Cart cart) {

        List<CartItemResponse> items = cart.getItems()
                .stream()
                .map(this::mapToCartItemResponse)
                .toList();

        BigDecimal totalAmount = items.stream()
                .map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return CartResponse.builder()
                .cartId(cart.getId())
                .userId(cart.getUser().getId())
                .items(items)
                .totalAmount(totalAmount)
                .build();
    }

    private CartItemResponse mapToCartItemResponse(
            CartItem item
    ) {

        BigDecimal subtotal =
                item.getPrice().multiply(item.getQuantity());

        return CartItemResponse.builder()
                .id(item.getId())
                .vegetableId(item.getVegetable().getId())
                .vegetableName(item.getVegetable().getName())
                .imageUrl(item.getVegetable().getImageUrl())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(subtotal)
                .build();
    }
}