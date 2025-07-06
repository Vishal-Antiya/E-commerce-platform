package com.turbo.orderservice.service;

import com.turbo.orderservice.model.Order;
import com.turbo.orderservice.model.OrderItem;
import com.turbo.orderservice.model.OrderStatus;
import com.turbo.orderservice.repository.OrderItemRepository;
import com.turbo.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    // --- Cart Management (Order with PENDING status) ---

    @Transactional
    public Order addProductToCart(String username, Long productId, int quantity) {
        logger.info("Adding product {} (quantity {}) to cart for user {}", productId, quantity, username);

        // 1. Get or create the user's pending cart
        Order cart = orderRepository.findByUsernameAndStatus(username, OrderStatus.PENDING)
                .orElseGet(() -> {
                    logger.debug("Creating new cart for user {}", username);
                    return orderRepository.save(new Order(username, OrderStatus.PENDING, BigDecimal.ZERO));
                });

        // 2. Simulate fetching product details from Product Service
        BigDecimal productPrice = getProductPriceFromProductService(productId);
        if (productPrice == null) {
            logger.error("Product with ID {} not found or price not available.", productId);
            throw new RuntimeException("Product not found or price unavailable.");
        }

        // 3. Check if the product already exists in the cart
        Optional<OrderItem> existingOrderItem = cart.getOrderItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst();
        if (existingOrderItem.isPresent()) {
            OrderItem item = existingOrderItem.get();
            item.setQuantity(item.getQuantity() + quantity);
            logger.debug("Updated quantity for product {} in cart. New quantity: {}", productId, item.getQuantity());
        } else {
            OrderItem newItem = new OrderItem(productId, quantity, productPrice);
            cart.addOrderItem(newItem);
            logger.debug("Added new product {} to cart with quantity {}", productId, quantity);
        }

        // 4. Recalculate total amount
        recalculateCartTotal(cart);
        logger.info("Product {} added to cart for user {}. Cart total: {}", productId, username, cart.getTotalAmount());
        return orderRepository.save(cart);
    }

    @Transactional
    public Order updateProductQuantityInCart(String username, Long productId, int newQuantity) {
        logger.info("Updating quantity of product {} to {} for user {}", productId, newQuantity, username);
        if (newQuantity <= 0) {
            return removeProductFromCart(username, productId);
        }
        Order cart = orderRepository.findByUsernameAndStatus(username, OrderStatus.PENDING)
                .orElseThrow(() -> {
                    logger.warn("Cart not found for user {}", username);
                    return new RuntimeException("Cart not found.");
                });
        OrderItem itemToUpdate = cart.getOrderItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warn("Product {} not found in cart for user {}", productId, username);
                    return new RuntimeException("Product not found in cart.");
                });
        itemToUpdate.setQuantity(newQuantity);
        recalculateCartTotal(cart);
        logger.info("Product {} quantity updated to {} for user {}. Cart total: {}", productId, newQuantity, username, cart.getTotalAmount());
        return orderRepository.save(cart);
    }

    @Transactional
    public Order removeProductFromCart(String username, Long productId) {
        logger.info("Removing product {} from cart for user {}", productId, username);
        Order cart = orderRepository.findByUsernameAndStatus(username, OrderStatus.PENDING)
                .orElseThrow(() -> {
                    logger.warn("Cart not found for user {}", username);
                    return new RuntimeException("Cart not found.");
                });
        OrderItem itemToRemove = cart.getOrderItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warn("Product {} not found in cart for user {}", productId, username);
                    return new RuntimeException("Product not found in cart.");
                });
        cart.removeOrderItem(itemToRemove);
        orderItemRepository.delete(itemToRemove);
        recalculateCartTotal(cart);
        logger.info("Product {} removed from cart for user {}. New cart total: {}", productId, username, cart.getTotalAmount());
        return orderRepository.save(cart);
    }

    public Optional<Order> getCart(String username) {
        logger.info("Fetching cart for user {}", username);
        Optional<Order> cart = orderRepository.findByUsernameAndStatus(username, OrderStatus.PENDING);
        if (cart.isPresent()) {
            logger.info("Cart found for user {}. Total items: {}", username, cart.get().getOrderItems().size());
        } else {
            logger.info("No active cart found for user {}", username);
        }
        return cart;
    }

    // --- Order Placement and History ---

    @Transactional
    public Order checkoutCart(String username) {
        logger.info("Initiating checkout for user {}", username);
        Order cart = orderRepository.findByUsernameAndStatus(username, OrderStatus.PENDING)
                .orElseThrow(() -> {
                    logger.warn("No pending cart found for user {} to checkout.", username);
                    return new RuntimeException("No pending cart to checkout.");
                });
        if (cart.getOrderItems().isEmpty()) {
            logger.warn("User {}'s cart is empty. Cannot checkout an empty cart.", username);
            throw new RuntimeException("Cannot checkout an empty cart.");
        }
        cart.setStatus(OrderStatus.PLACED);
        Order placedOrder = orderRepository.save(cart);
        logger.info("Cart for user {} checked out successfully. New Order ID: {}", username, placedOrder.getId());
        return placedOrder;
    }

    public Optional<Order> getOrderById(Long orderId, String username) {
        logger.info("Fetching order {} for user {}", orderId, username);
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent() && order.get().getUsername().equals(username)) {
            logger.info("Order {} found for user {}", orderId, username);
            return order;
        }
        logger.warn("Order {} not found or does not belong to user {}.", orderId, username);
        return Optional.empty();
    }

    public List<Order> getOrdersByUserId(String username) {
        logger.info("Fetching all orders for user {}", username);
        List<Order> orders = orderRepository.findByUsername(username);
        logger.info("Found {} orders for user {}", orders.size(), username);
        return orders;
    }

    // --- Helper Methods ---

    private void recalculateCartTotal(Order cart) {
        BigDecimal total = cart.getOrderItems().stream()
                .map(item -> item.getPriceAtPurchase().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalAmount(total);
        logger.debug("Recalculated total for cart {}: {}", cart.getId(), total);
    }

    // This method simulates a call to the Product Service
    private BigDecimal getProductPriceFromProductService(Long productId) {
        logger.debug("Simulating call to Product Service for product ID: {}", productId);
        if (productId.equals(1L)) {
            return new BigDecimal("10.00");
        } else if (productId.equals(2L)) {
            return new BigDecimal("25.50");
        } else if (productId.equals(3L)) {
            return new BigDecimal("5.00");
        }
        return null; // Product not found
    }

    public List<Order> getAllOrders() {
        logger.info("Fetching all orders from repository");
        return orderRepository.findAll();
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(OrderStatus.valueOf(newStatus));
        return orderRepository.save(order);
    }

}
