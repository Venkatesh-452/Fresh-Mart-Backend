package com.vegetablemart.backend.repository;

import com.vegetablemart.backend.entity.Order;
import com.vegetablemart.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserOrderByOrderDateDesc(User user);

    List<Order> findByUserIdOrderByOrderDateDesc(Long userId);
}