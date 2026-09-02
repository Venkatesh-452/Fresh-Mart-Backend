package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.cart.AddToCartRequest;
import com.vegetablemart.backend.dto.cart.CartItemResponse;
import com.vegetablemart.backend.dto.cart.CartResponse;
import com.vegetablemart.backend.dto.cart.UpdateCartItemRequest;
import com.vegetablemart.backend.entity.Cart;
import com.vegetablemart.backend.entity.CartItem;
import com.vegetablemart.backend.entity.User;
import com.vegetablemart.backend.entity.Vegetable;
import com.vegetablemart.backend.exception.BadRequestException;
import com.vegetablemart.backend.exception.ResourceNotFoundException;
import com.vegetablemart.backend.repository.CartItemRepository;
import com.vegetablemart.backend.repository.CartRepository;
import com.vegetablemart.backend.repository.InventoryRepository;
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
    private final InventoryRepository inventoryRepository;
    private final VegetableRepository vegetableRepository;
    private final UserRepository userRepository;

    @Override
    public CartResponse addToCart(String email, AddToCartRequest request) {
        validateRequest(request);
        User user = getUserByEmail(email);
        Vegetable vegetable = getActiveVegetable(request.getVegetableId());
        validateStock(vegetable, request.getQuantity());

        Cart cart = getOrCreateCart(user);
        CartItem cartItem = cartItemRepository
                .findByCartIdAndVegetableId(cart.getId(), vegetable.getId())
                .orElse(null);

        if (cartItem == null) {
            cartItem = CartItem.builder().cart(cart).vegetable(vegetable)
                    .quantity(request.getQuantity()).price(vegetable.getPrice()).build();
        } else {
            BigDecimal newQuantity = cartItem.getQuantity().add(request.getQuantity());
            validateStock(vegetable, newQuantity);
            cartItem.setQuantity(newQuantity);
            cartItem.setPrice(vegetable.getPrice());
        }

        cartItemRepository.save(cartItem);
        return buildCartResponse(cart);
    }

    @Override
    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        User user = getUserByEmail(email);
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> Cart.builder().user(user).items(List.of()).build());
        return buildCartResponse(cart);
    }

    @Override
    public CartResponse updateCartItem(String email, Long cartItemId, UpdateCartItemRequest request) {
        validateId(cartItemId, "Cart item ID");
        validateRequest(request);
        User user = getUserByEmail(email);
        Cart cart = getUserCart(user);
        CartItem cartItem = getCartItemForUser(cartItemId, cart);
        Vegetable vegetable = getActiveVegetable(cartItem.getVegetable().getId());
        validateStock(vegetable, request.getQuantity());
        cartItem.setQuantity(request.getQuantity());
        cartItem.setPrice(vegetable.getPrice());
        cartItemRepository.save(cartItem);
        return buildCartResponse(cart);
    }

    @Override
    public void removeCartItem(String email, Long cartItemId) {
        validateId(cartItemId, "Cart item ID");
        User user = getUserByEmail(email);
        Cart cart = getUserCart(user);
        CartItem cartItem = getCartItemForUser(cartItemId, cart);
        cart.removeItem(cartItem);
        cartItemRepository.delete(cartItem);
    }

    @Override
    public void clearCart(String email) {
        User user = getUserByEmail(email);
        Cart cart = getUserCart(user);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    private User getUserByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Authenticated user email is required");
        }
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private Cart getOrCreateCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(Cart.builder().user(user).build()));
    }

    private Cart getUserCart(User user) {
        return cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));
    }

    private CartItem getCartItemForUser(Long cartItemId, Cart cart) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item not found"));
        if (cartItem.getCart() == null || !cartItem.getCart().getId().equals(cart.getId())) {
            throw new BadRequestException("Cart item does not belong to this user");
        }
        return cartItem;
    }

    private Vegetable getActiveVegetable(Long vegetableId) {
        validateId(vegetableId, "Vegetable ID");
        Vegetable vegetable = vegetableRepository.findById(vegetableId)
                .orElseThrow(() -> new ResourceNotFoundException("Vegetable not found with ID: " + vegetableId));
        if (!Boolean.TRUE.equals(vegetable.getActive())) {
            throw new BadRequestException("Vegetable is not available");
        }
        return vegetable;
    }

    private void validateStock(Vegetable vegetable, BigDecimal requestedQuantity) {
        BigDecimal availableQuantity = inventoryRepository.findByVegetableId(vegetable.getId())
                .map(inventory -> inventory.getAvailableQuantity()).orElse(vegetable.getQuantity());
        if (availableQuantity == null) availableQuantity = BigDecimal.ZERO;
        if (requestedQuantity.compareTo(availableQuantity) > 0) {
            throw new BadRequestException("Insufficient stock. Available quantity: " + availableQuantity);
        }
    }

    private void validateRequest(AddToCartRequest request) {
        if (request == null) throw new BadRequestException("Cart request is required");
        if (request.getVegetableId() == null || request.getVegetableId() <= 0)
            throw new BadRequestException("Vegetable ID must be positive");
        if (request.getQuantity() == null || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Quantity must be greater than 0");
    }

    private void validateRequest(UpdateCartItemRequest request) {
        if (request == null || request.getQuantity() == null
                || request.getQuantity().compareTo(BigDecimal.ZERO) <= 0)
            throw new BadRequestException("Quantity must be greater than 0");
    }

    private void validateId(Long id, String fieldName) {
        if (id == null || id <= 0) throw new BadRequestException(fieldName + " must be positive");
    }

    private CartResponse buildCartResponse(Cart cart) {
        List<CartItemResponse> items = cart.getItems().stream().map(this::mapToCartItemResponse).toList();
        BigDecimal totalAmount = items.stream().map(CartItemResponse::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return CartResponse.builder().cartId(cart.getId()).userId(cart.getUser().getId())
                .items(items).totalAmount(totalAmount).build();
    }

    private CartItemResponse mapToCartItemResponse(CartItem item) {
        BigDecimal subtotal = item.getPrice().multiply(item.getQuantity());
        return CartItemResponse.builder().id(item.getId()).vegetableId(item.getVegetable().getId())
                .vegetableName(item.getVegetable().getName()).imageUrl(item.getVegetable().getImageUrl())
                .quantity(item.getQuantity()).price(item.getPrice()).subtotal(subtotal).build();
    }
}
