package com.vegetablemart.backend.service;

import com.vegetablemart.backend.dto.payment.CreatePaymentRequest;
import com.vegetablemart.backend.dto.payment.PaymentResponse;
import com.vegetablemart.backend.dto.payment.PaymentStatusRequest;

import java.util.List;

public interface PaymentService {

    PaymentResponse createPayment(String email, CreatePaymentRequest request);

    PaymentResponse getPaymentByOrder(String email, Long orderId);

    List<PaymentResponse> getMyPayments(String email);

    List<PaymentResponse> getAllPayments();

    List<PaymentResponse> getPendingPayments();

    PaymentResponse updatePaymentStatus(Long paymentId, PaymentStatusRequest request);
}
