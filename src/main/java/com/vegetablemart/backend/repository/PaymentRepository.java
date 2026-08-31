package com.vegetablemart.backend.repository;

import com.vegetablemart.backend.entity.Payment;
import com.vegetablemart.backend.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatusOrderByCreatedAtDesc(
            PaymentStatus status
    );

    boolean existsByOrderId(Long orderId);

    Optional<Payment> findByTransactionId(String transactionId);
}