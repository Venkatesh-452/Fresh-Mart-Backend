package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.order.OrderResponse;
import com.vegetablemart.backend.dto.order.PlaceOrderRequest;

import java.util.List;

public interface OrderService {

    OrderResponse placeOrder(String email, PlaceOrderRequest request);

    List<OrderResponse> getMyOrders(String email);

    OrderResponse getOrderById(String email, Long orderId);

    List<OrderResponse> getAllOrders();

    OrderResponse updateOrderStatus(Long orderId, String status);

    void restoreStock(Long orderId);
}
