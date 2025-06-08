package com.turbo.orderservice.repository;

import com.turbo.orderservice.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // Find an order item by product ID within a specific order
    Optional<OrderItem> findByOrderIdAndProductId(Long orderId, Long productId);
}
