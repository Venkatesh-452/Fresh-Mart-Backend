package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.order.OrderItemResponse;
import com.vegetablemart.backend.dto.order.OrderResponse;
import com.vegetablemart.backend.dto.order.PlaceOrderRequest;
import com.vegetablemart.backend.entity.Address;
import com.vegetablemart.backend.entity.Cart;
import com.vegetablemart.backend.entity.CartItem;
import com.vegetablemart.backend.entity.Inventory;
import com.vegetablemart.backend.entity.Order;
import com.vegetablemart.backend.entity.OrderItem;
import com.vegetablemart.backend.entity.OrderStatus;
import com.vegetablemart.backend.entity.User;
import com.vegetablemart.backend.entity.Vegetable;
import com.vegetablemart.backend.exception.BadRequestException;
import com.vegetablemart.backend.exception.ResourceNotFoundException;
import com.vegetablemart.backend.repository.AddressRepository;
import com.vegetablemart.backend.repository.CartRepository;
import com.vegetablemart.backend.repository.InventoryRepository;
import com.vegetablemart.backend.repository.OrderRepository;
import com.vegetablemart.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final InventoryRepository inventoryRepository;

    @Override
    public OrderResponse placeOrder(String email, PlaceOrderRequest request) {
        User user = getActiveUser(email);
        if (request == null || request.getAddressId() == null)
            throw new BadRequestException("Address ID is required");
        Address address = addressRepository.findByIdAndUserId(request.getAddressId(), user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Address not found or does not belong to the user"));
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found for user"));
        if (cart.getItems() == null || cart.getItems().isEmpty())
            throw new BadRequestException("Cannot place order. Cart is empty");

        Order order = Order.builder().user(user).status(OrderStatus.PLACED).totalAmount(BigDecimal.ZERO)
                .deliveryFullName(address.getFullName()).deliveryPhone(address.getPhone())
                .deliveryAddressLine(address.getAddressLine()).deliveryCity(address.getCity())
                .deliveryState(address.getState()).deliveryPincode(address.getPincode())
                .deliveryLandmark(address.getLandmark()).build();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Vegetable vegetable = cartItem.getVegetable();
            if (vegetable == null || vegetable.getId() == null)
                throw new BadRequestException("Invalid product in cart");
            if (!Boolean.TRUE.equals(vegetable.getActive()))
                throw new BadRequestException(vegetable.getName() + " is currently unavailable");
            Inventory inventory = inventoryRepository.findByVegetableId(vegetable.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for vegetable: " + vegetable.getName()));
            BigDecimal requestedQuantity = cartItem.getQuantity();
            BigDecimal availableQuantity = inventory.getAvailableQuantity();
            if (requestedQuantity == null || requestedQuantity.signum() <= 0)
                throw new BadRequestException("Invalid quantity for " + vegetable.getName());
            if (availableQuantity == null || requestedQuantity.compareTo(availableQuantity) > 0)
                throw new BadRequestException("Insufficient stock for " + vegetable.getName() + ". Available: " + availableQuantity);
            BigDecimal price = vegetable.getPrice();
            if (price == null || price.signum() < 0)
                throw new BadRequestException("Invalid price for " + vegetable.getName());
            BigDecimal subtotal = price.multiply(requestedQuantity);
            order.getItems().add(OrderItem.builder().order(order).vegetable(vegetable)
                    .quantity(requestedQuantity).price(price).subtotal(subtotal).build());
            totalAmount = totalAmount.add(subtotal);
            inventory.setAvailableQuantity(availableQuantity.subtract(requestedQuantity));
            inventory.setSoldQuantity(inventory.getSoldQuantity().add(requestedQuantity));
            vegetable.setQuantity(inventory.getAvailableQuantity());
        }
        order.setTotalAmount(totalAmount);
        Order savedOrder = orderRepository.save(order);
        cart.getItems().clear();
        cartRepository.save(cart);
        return mapToOrderResponse(savedOrder);
    }

    @Override @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String email) {
        return orderRepository.findByUserOrderByOrderDateDesc(getActiveUser(email)).stream()
                .map(this::mapToOrderResponse).toList();
    }

    @Override @Transactional(readOnly = true)
    public OrderResponse getOrderById(String email, Long orderId) {
        User user = getActiveUser(email);
        validateId(orderId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        if (!order.getUser().getId().equals(user.getId()))
            throw new BadRequestException("You are not authorized to view this order");
        return mapToOrderResponse(order);
    }

    @Override @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream().map(this::mapToOrderResponse).toList();
    }

    @Override
    public OrderResponse updateOrderStatus(Long orderId, String status) {
        validateId(orderId);
        if (status == null || status.isBlank()) throw new BadRequestException("Order status is required");
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));
        final OrderStatus newStatus;
        try { newStatus = OrderStatus.valueOf(status.trim().toUpperCase()); }
        catch (IllegalArgumentException e) { throw new BadRequestException("Invalid order status: " + status); }
        OrderStatus currentStatus = order.getStatus();
        if (currentStatus == OrderStatus.CANCELLED) throw new BadRequestException("Cancelled order cannot be updated");
        if (currentStatus == OrderStatus.DELIVERED && newStatus != OrderStatus.DELIVERED)
            throw new BadRequestException("Delivered order cannot be moved to another status");
        if (newStatus == OrderStatus.CANCELLED) restoreStock(order);
        order.setStatus(newStatus);
        return mapToOrderResponse(orderRepository.save(order));
    }

    private void restoreStock(Order order) {
        for (OrderItem item : order.getItems()) {
            Vegetable vegetable = item.getVegetable();
            Inventory inventory = inventoryRepository.findByVegetableId(vegetable.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for vegetable: " + vegetable.getName()));
            inventory.setAvailableQuantity(inventory.getAvailableQuantity().add(item.getQuantity()));
            inventory.setSoldQuantity(inventory.getSoldQuantity().subtract(item.getQuantity()));
            vegetable.setQuantity(inventory.getAvailableQuantity());
        }
    }

    private User getActiveUser(String email) {
        if (email == null || email.isBlank()) throw new BadRequestException("Authenticated user not found");
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (!Boolean.TRUE.equals(user.getActive())) throw new BadRequestException("User account is inactive");
        return user;
    }

    private void validateId(Long id) { if (id == null || id <= 0) throw new BadRequestException("Order ID must be positive"); }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> items = order.getItems().stream().map(this::mapToOrderItemResponse).toList();
        String deliveryAddress = order.getDeliveryAddressLine() + ", " + order.getDeliveryCity() + ", "
                + order.getDeliveryState() + " - " + order.getDeliveryPincode();
        return OrderResponse.builder().orderId(order.getId()).userId(order.getUser().getId())
                .customerName(order.getUser().getName()).totalAmount(order.getTotalAmount()).status(order.getStatus())
                .orderDate(order.getOrderDate()).deliveryFullName(order.getDeliveryFullName())
                .deliveryPhone(order.getDeliveryPhone()).deliveryAddress(deliveryAddress)
                .deliveryCity(order.getDeliveryCity()).deliveryState(order.getDeliveryState())
                .deliveryPincode(order.getDeliveryPincode()).deliveryLandmark(order.getDeliveryLandmark())
                .items(items).build();
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem item) {
        return OrderItemResponse.builder().id(item.getId()).vegetableId(item.getVegetable().getId())
                .vegetableName(item.getVegetable().getName()).imageUrl(item.getVegetable().getImageUrl())
                .quantity(item.getQuantity()).price(item.getPrice()).subtotal(item.getSubtotal()).build();
    }
}
