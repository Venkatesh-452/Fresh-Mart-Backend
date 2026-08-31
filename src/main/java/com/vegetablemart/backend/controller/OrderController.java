package com.vegetablemart.backend.controller;

import com.vegetablemart.backend.dto.order.OrderResponse;
import com.vegetablemart.backend.dto.order.PlaceOrderRequest;
import com.vegetablemart.backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    // =========================
    // USER APIs
    // =========================

    @PostMapping("/place")
    public ResponseEntity<OrderResponse> placeOrder(
            @RequestParam Long userId,
            @RequestBody(required = false) PlaceOrderRequest request
    ) {

        if (request == null) {
            request = new PlaceOrderRequest();
        }

        return ResponseEntity.ok(
                orderService.placeOrder(userId, request)
        );
    }

    @GetMapping("/my-orders")
    public ResponseEntity<List<OrderResponse>> getMyOrders(
            @RequestParam Long userId
    ) {

        return ResponseEntity.ok(
                orderService.getMyOrders(userId)
        );
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrderById(
            @RequestParam Long userId,
            @PathVariable Long orderId
    ) {

        return ResponseEntity.ok(
                orderService.getOrderById(
                        userId,
                        orderId
                )
        );
    }

    // =========================
    // ADMIN APIs
    // =========================

    @GetMapping("/all")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {

        return ResponseEntity.ok(
                orderService.getAllOrders()
        );
    }

    @PutMapping("/{orderId}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long orderId,
            @RequestParam String status
    ) {

        return ResponseEntity.ok(
                orderService.updateOrderStatus(
                        orderId,
                        status
                )
        );
    }
}