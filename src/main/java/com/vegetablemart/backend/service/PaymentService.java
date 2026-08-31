package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.payment.CreatePaymentRequest;
import com.vegetablemart.backend.dto.payment.PaymentResponse;
import com.vegetablemart.backend.dto.payment.PaymentStatusRequest;

import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(
            Long userId,
            CreatePaymentRequest request
    );

    PaymentResponse getPaymentByOrder(
            Long userId,
            Long orderId
    );

    List<PaymentResponse> getMyPayments(Long userId);

    List<PaymentResponse> getAllPayments();

    List<PaymentResponse> getPendingPayments();

    PaymentResponse updatePaymentStatus(
            Long paymentId,
            PaymentStatusRequest request
    );
}