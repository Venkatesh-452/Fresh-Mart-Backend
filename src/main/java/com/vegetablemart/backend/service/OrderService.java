package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.order.OrderResponse;
import com.vegetablemart.backend.dto.order.PlaceOrderRequest;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(
            Long userId,
            PlaceOrderRequest request
    );

    List<OrderResponse> getMyOrders(Long userId);

    OrderResponse getOrderById(
            Long userId,
            Long orderId
    );

    List<OrderResponse> getAllOrders();

    OrderResponse updateOrderStatus(
            Long orderId,
            String status
    );
}