package com.vegetablemart.backend.controller;

import com.vegetablemart.backend.dto.payment.CreatePaymentRequest;
import com.vegetablemart.backend.dto.payment.PaymentResponse;
import com.vegetablemart.backend.dto.payment.PaymentStatusRequest;
import com.vegetablemart.backend.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    // =========================
    // USER APIs
    // =========================

    @PostMapping("/create")
    public ResponseEntity<PaymentResponse> createPayment(
            @RequestParam Long userId,
            @Valid @RequestBody CreatePaymentRequest request
    ) {

        return ResponseEntity.ok(
                paymentService.createPayment(
                        userId,
                        request
                )
        );
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<PaymentResponse> getPaymentByOrder(
            @RequestParam Long userId,
            @PathVariable Long orderId
    ) {

        return ResponseEntity.ok(
                paymentService.getPaymentByOrder(
                        userId,
                        orderId
                )
        );
    }

    @GetMapping("/my-payments")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(
            @RequestParam Long userId
    ) {

        return ResponseEntity.ok(
                paymentService.getMyPayments(userId)
        );
    }

    // =========================
    // ADMIN APIs
    // =========================

    @GetMapping("/all")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {

        return ResponseEntity.ok(
                paymentService.getAllPayments()
        );
    }

    @GetMapping("/pending")
    public ResponseEntity<List<PaymentResponse>> getPendingPayments() {

        return ResponseEntity.ok(
                paymentService.getPendingPayments()
        );
    }

    @PutMapping("/{paymentId}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable Long paymentId,
            @Valid @RequestBody PaymentStatusRequest request
    ) {

        return ResponseEntity.ok(
                paymentService.updatePaymentStatus(
                        paymentId,
                        request
                )
        );
    }
}