package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.order.OrderItemResponse;
import com.vegetablemart.backend.dto.order.OrderResponse;
import com.vegetablemart.backend.dto.order.PlaceOrderRequest;
import com.vegetablemart.backend.entity.*;
import com.vegetablemart.backend.repository.*;
import com.vegetablemart.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.vegetablemart.backend.repository.InventoryRepository;


import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final VegetableRepository vegetableRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public OrderResponse placeOrder(
            Long userId,
            PlaceOrderRequest request
    ) {

        // 1. Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with ID: " + userId
                        )
                );

        // 2. Find user's cart
        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Cart not found for user"
                        )
                );

        // 3. Check cart is not empty
        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new RuntimeException(
                    "Cannot place order. Cart is empty"
            );
        }

        // 4. Create Order
        Order order = Order.builder()
                .user(user)
                .status(OrderStatus.PLACED)
                .totalAmount(BigDecimal.ZERO)
                .build();

        orderRepository.save(order);

        BigDecimal totalAmount = BigDecimal.ZERO;

        // 5. Process every cart item
        for (CartItem cartItem : cart.getItems()) {

            Vegetable vegetable = vegetableRepository.findById(
                    cartItem.getVegetable().getId()
            ).orElseThrow(() ->
                    new RuntimeException(
                            "Vegetable not found with ID: "
                                    + cartItem.getVegetable().getId()
                    )
            );

            // 6. Check current stock
            if (!Boolean.TRUE.equals(vegetable.getActive())) {
                throw new RuntimeException(
                        vegetable.getName()
                                + " is currently unavailable"
                );
            }

            if (cartItem.getQuantity()
                    .compareTo(vegetable.getQuantity()) > 0) {

                throw new RuntimeException(
                        "Insufficient stock for "
                                + vegetable.getName()
                                + ". Available: "
                                + vegetable.getQuantity()
                );
            }

            // 7. Calculate subtotal
            BigDecimal subtotal =
                    cartItem.getPrice()
                            .multiply(cartItem.getQuantity());

            // 8. Create OrderItem
            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .vegetable(vegetable)
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice())
                    .subtotal(subtotal)
                    .build();

            orderItemRepository.save(orderItem);

            order.getItems().add(orderItem);

            // 9. Add to total
            totalAmount = totalAmount.add(subtotal);

            // 10. Reduce stock
            BigDecimal remainingStock =
                    vegetable.getQuantity()
                            .subtract(cartItem.getQuantity());

            vegetable.setQuantity(remainingStock);

            vegetableRepository.save(vegetable);
            Inventory inventory = inventoryRepository
                    .findByVegetableId(vegetable.getId())
                    .orElseThrow(() ->
                            new RuntimeException(
                                    "Inventory not found for vegetable: "
                                            + vegetable.getName()
                            )
                    );

            BigDecimal newSoldQuantity =
                    inventory.getSoldQuantity()
                            .add(cartItem.getQuantity());

            BigDecimal newAvailableQuantity =
                    inventory.getAvailableQuantity()
                            .subtract(cartItem.getQuantity());

            inventory.setSoldQuantity(newSoldQuantity);
            inventory.setAvailableQuantity(newAvailableQuantity);

            inventoryRepository.save(inventory);
        }

        // 11. Set final total
        order.setTotalAmount(totalAmount);

        // 12. Save order
        orderRepository.save(order);

        // 13. Clear cart
        cart.getItems().clear();

        cartRepository.save(cart);

        // 14. Return response
        return mapToOrderResponse(
                order,
                request != null
                        ? request.getDeliveryAddress()
                        : null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(Long userId) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with ID: " + userId
                        )
                );

        return orderRepository
                .findByUserIdOrderByOrderDateDesc(userId)
                .stream()
                .map(order -> mapToOrderResponse(order, null))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(
            Long userId,
            Long orderId
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found with ID: "
                                        + orderId
                        )
                );

        // Make sure order belongs to this user
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to view this order"
            );
        }

        return mapToOrderResponse(order, null);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {

        return orderRepository.findAll()
                .stream()
                .map(order -> mapToOrderResponse(order, null))
                .toList();
    }

    @Override
    public OrderResponse updateOrderStatus(
            Long orderId,
            String status
    ) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Order not found with ID: "
                                        + orderId
                        )
                );

        OrderStatus orderStatus;

        try {
            orderStatus = OrderStatus.valueOf(
                    status.toUpperCase()
            );
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(
                    "Invalid order status: " + status
            );
        }

        order.setStatus(orderStatus);

        orderRepository.save(order);

        return mapToOrderResponse(order, null);
    }

    private OrderResponse mapToOrderResponse(
            Order order,
            String deliveryAddress
    ) {

        List<OrderItemResponse> items =
                order.getItems()
                        .stream()
                        .map(this::mapToOrderItemResponse)
                        .toList();

        return OrderResponse.builder()
                .orderId(order.getId())
                .userId(order.getUser().getId())
                .customerName(order.getUser().getName())
                .totalAmount(order.getTotalAmount())
                .status(order.getStatus())
                .orderDate(order.getOrderDate())
                .deliveryAddress(deliveryAddress)
                .items(items)
                .build();
    }

    private OrderItemResponse mapToOrderItemResponse(
            OrderItem item
    ) {

        return OrderItemResponse.builder()
                .id(item.getId())
                .vegetableId(item.getVegetable().getId())
                .vegetableName(item.getVegetable().getName())
                .imageUrl(item.getVegetable().getImageUrl())
                .quantity(item.getQuantity())
                .price(item.getPrice())
                .subtotal(item.getSubtotal())
                .build();
    }
}