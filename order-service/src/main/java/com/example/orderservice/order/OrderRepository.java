package com.example.orderservice.order;

import com.example.orderservice.order.domain.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserId(Long userId);
    Optional<Order> findByOrderId(String orderId);
}
