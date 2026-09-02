package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.payment.CreatePaymentRequest;
import com.vegetablemart.backend.dto.payment.PaymentResponse;
import com.vegetablemart.backend.dto.payment.PaymentStatusRequest;
import com.vegetablemart.backend.entity.*;
import com.vegetablemart.backend.exception.BadRequestException;
import com.vegetablemart.backend.exception.DuplicateResourceException;
import com.vegetablemart.backend.exception.ForbiddenException;
import com.vegetablemart.backend.exception.ResourceNotFoundException;
import com.vegetablemart.backend.repository.OrderRepository;
import com.vegetablemart.backend.repository.PaymentRepository;
import com.vegetablemart.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service @RequiredArgsConstructor @Transactional
public class PaymentServiceImpl implements PaymentService {
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;

    @Override
    public PaymentResponse createPayment(String email, CreatePaymentRequest request) {
        if (request == null || request.getOrderId() == null || request.getOrderId() <= 0)
            throw new BadRequestException("Valid order ID is required");
        if (request.getPaymentMethod() == null) throw new BadRequestException("Payment method is required");
        User user = getUserByEmail(email);
        Order order = orderRepository.findById(request.getOrderId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + request.getOrderId()));
        if (!order.getUser().getId().equals(user.getId()))
            throw new ForbiddenException("You are not authorized to make payment for this order");
        if (order.getStatus() == OrderStatus.CANCELLED)
            throw new BadRequestException("Cannot create payment for a cancelled order");
        if (paymentRepository.existsByOrderId(order.getId()))
            throw new DuplicateResourceException("Payment already exists for this order");

        Payment payment = Payment.builder().order(order).user(user).amount(order.getTotalAmount())
                .paymentMethod(request.getPaymentMethod()).status(PaymentStatus.PENDING).build();
        return mapToResponse(paymentRepository.save(payment));
    }

    @Override @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrder(String email, Long orderId) {
        User user = getUserByEmail(email); validateId(orderId);
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found for order ID: " + orderId));
        if (!payment.getUser().getId().equals(user.getId()))
            throw new ForbiddenException("You are not authorized to view this payment");
        return mapToResponse(payment);
    }

    @Override @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments(String email) {
        User user = getUserByEmail(email);
        return paymentRepository.findByUserIdOrderByCreatedAtDesc(user.getId()).stream().map(this::mapToResponse).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Override @Transactional(readOnly = true)
    public List<PaymentResponse> getPendingPayments() {
        return paymentRepository.findByStatusOrderByCreatedAtDesc(PaymentStatus.PENDING).stream().map(this::mapToResponse).toList();
    }

    @Override
    public PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatusRequest request) {
        validateId(paymentId);
        if (request == null || request.getStatus() == null) throw new BadRequestException("Payment status is required");
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with ID: " + paymentId));
        validateStatusUpdate(payment, request);
        if (request.getTransactionId() != null && !request.getTransactionId().trim().isEmpty()) {
            String transactionId = request.getTransactionId().trim();
            paymentRepository.findByTransactionId(transactionId).ifPresent(existing -> {
                if (!existing.getId().equals(payment.getId()))
                    throw new DuplicateResourceException("Transaction ID already belongs to another payment");
            });
            payment.setTransactionId(transactionId);
        }
        payment.setStatus(request.getStatus());
        if (request.getStatus() == PaymentStatus.SUCCESS && payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
            if (payment.getOrder().getStatus() == OrderStatus.PLACED) payment.getOrder().setStatus(OrderStatus.CONFIRMED);
        }
        if (request.getStatus() == PaymentStatus.REFUNDED) payment.getOrder().setStatus(OrderStatus.CANCELLED);
        return mapToResponse(paymentRepository.save(payment));
    }

    private User getUserByEmail(String email) {
        if (email == null || email.isBlank()) throw new BadRequestException("Authenticated user is required");
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    private void validateStatusUpdate(Payment payment, PaymentStatusRequest request) {
        PaymentStatus current = payment.getStatus(), next = request.getStatus();
        if (current == PaymentStatus.REFUNDED) throw new BadRequestException("Refunded payment status cannot be changed");
        if (current == PaymentStatus.SUCCESS && next == PaymentStatus.PENDING)
            throw new BadRequestException("Successful payment cannot be changed back to pending");
        if (next == PaymentStatus.SUCCESS && payment.getPaymentMethod() == PaymentMethod.ONLINE
                && (request.getTransactionId() == null || request.getTransactionId().trim().isEmpty()))
            throw new BadRequestException("Transaction ID is required for successful online payment");
        if (next == PaymentStatus.REFUNDED && current != PaymentStatus.SUCCESS)
            throw new BadRequestException("Only successful payments can be refunded");
    }

    private void validateId(Long id) { if (id == null || id <= 0) throw new BadRequestException("Payment ID must be positive"); }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder().paymentId(payment.getId()).orderId(payment.getOrder().getId())
                .userId(payment.getUser().getId()).customerName(payment.getUser().getName()).amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod()).status(payment.getStatus()).transactionId(payment.getTransactionId())
                .paymentDate(payment.getPaymentDate()).createdAt(payment.getCreatedAt()).build();
    }
}
