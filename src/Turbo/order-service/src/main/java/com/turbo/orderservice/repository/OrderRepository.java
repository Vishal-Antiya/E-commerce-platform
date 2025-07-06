package com.turbo.orderservice.repository;

import com.turbo.orderservice.model.Order;
import com.turbo.orderservice.model.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // Find a user's pending cart
    Optional<Order> findByUsernameAndStatus(String username, OrderStatus status);

    // Find all orders for a specific user
    List<Order> findByUsername(String username);
}
