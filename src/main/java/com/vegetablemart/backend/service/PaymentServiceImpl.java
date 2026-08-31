package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.payment.CreatePaymentRequest;
import com.vegetablemart.backend.dto.payment.PaymentResponse;
import com.vegetablemart.backend.dto.payment.PaymentStatusRequest;
import com.vegetablemart.backend.entity.*;
import com.vegetablemart.backend.repository.OrderRepository;
import com.vegetablemart.backend.repository.PaymentRepository;
import com.vegetablemart.backend.repository.UserRepository;
import com.vegetablemart.backend.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    public PaymentResponse createPayment(
            Long userId,
            CreatePaymentRequest request
    ) {

        // 1. Find user
        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with ID: " + userId
                        )
                );

        // 2. Find order
        Order order = orderRepository.findById(
                request.getOrderId()
        ).orElseThrow(() ->
                new RuntimeException(
                        "Order not found with ID: "
                                + request.getOrderId()
                )
        );

        // 3. Check order belongs to user
        if (!order.getUser().getId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to make payment for this order"
            );
        }

        // 4. Check if payment already exists
        if (paymentRepository.existsByOrderId(order.getId())) {
            throw new RuntimeException(
                    "Payment already exists for this order"
            );
        }

        // 5. Create payment
        Payment payment = Payment.builder()
                .order(order)
                .user(user)
                .amount(order.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .build();

        // 6. Save
        payment = paymentRepository.save(payment);

        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrder(
            Long userId,
            Long orderId
    ) {

        Payment payment = paymentRepository
                .findByOrderId(orderId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment not found for order ID: "
                                        + orderId
                        )
                );

        if (!payment.getUser().getId().equals(userId)) {
            throw new RuntimeException(
                    "You are not authorized to view this payment"
            );
        }

        return mapToResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments(
            Long userId
    ) {

        userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found with ID: " + userId
                        )
                );

        return paymentRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {

        return paymentRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPendingPayments() {

        return paymentRepository
                .findByStatusOrderByCreatedAtDesc(
                        PaymentStatus.PENDING
                )
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public PaymentResponse updatePaymentStatus(
            Long paymentId,
            PaymentStatusRequest request
    ) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Payment not found with ID: "
                                        + paymentId
                        )
                );

        payment.setStatus(request.getStatus());

        if (request.getTransactionId() != null
                && !request.getTransactionId().isBlank()) {

            payment.setTransactionId(
                    request.getTransactionId()
            );
        }

        if (request.getStatus() == PaymentStatus.SUCCESS) {
            payment.setPaymentDate(LocalDateTime.now());

            payment.getOrder().setStatus(
                    OrderStatus.CONFIRMED
            );
        }

        paymentRepository.save(payment);

        return mapToResponse(payment);
    }

    private PaymentResponse mapToResponse(
            Payment payment
    ) {

        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrder().getId())
                .userId(payment.getUser().getId())
                .customerName(payment.getUser().getName())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}