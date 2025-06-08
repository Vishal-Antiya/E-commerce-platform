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
    public Order addProductToCart(Long userId, Long productId, int quantity) {
        logger.info("Adding product {} (quantity {}) to cart for user {}", productId, quantity, userId);

        // 1. Get or create the user's pending cart
        Order cart = orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING)
                .orElseGet(() -> {
                    logger.debug("Creating new cart for user {}", userId);
                    return orderRepository.save(new Order(userId, OrderStatus.PENDING, BigDecimal.ZERO));
                });

        // 2. Simulate fetching product details from Product Service
        // In a real microservice setup, you would use WebClient or RestTemplate to call product-service
        // For now, we'll use a dummy product price
        BigDecimal productPrice = getProductPriceFromProductService(productId); // Simulate call to Product Service
        if (productPrice == null) {
            logger.error("Product with ID {} not found or price not available.", productId);
            throw new RuntimeException("Product not found or price unavailable."); // Or a more specific exception
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
        logger.info("Product {} added to cart for user {}. Cart total: {}", productId, userId, cart.getTotalAmount());
        return orderRepository.save(cart);
    }

    @Transactional
    public Order updateProductQuantityInCart(Long userId, Long productId, int newQuantity) {
        logger.info("Updating quantity of product {} to {} for user {}", productId, newQuantity, userId);
        if (newQuantity <= 0) {
            return removeProductFromCart(userId, productId);
        }

        Order cart = orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING)
                .orElseThrow(() -> {
                    logger.warn("Cart not found for user {}", userId);
                    return new RuntimeException("Cart not found.");
                });

        OrderItem itemToUpdate = cart.getOrderItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warn("Product {} not found in cart for user {}", productId, userId);
                    return new RuntimeException("Product not found in cart.");
                });

        itemToUpdate.setQuantity(newQuantity);
        recalculateCartTotal(cart);
        logger.info("Product {} quantity updated to {} for user {}. Cart total: {}", productId, newQuantity, userId, cart.getTotalAmount());
        return orderRepository.save(cart);
    }

    @Transactional
    public Order removeProductFromCart(Long userId, Long productId) {
        logger.info("Removing product {} from cart for user {}", productId, userId);
        Order cart = orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING)
                .orElseThrow(() -> {
                    logger.warn("Cart not found for user {}", userId);
                    return new RuntimeException("Cart not found.");
                });

        OrderItem itemToRemove = cart.getOrderItems().stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> {
                    logger.warn("Product {} not found in cart for user {}", productId, userId);
                    return new RuntimeException("Product not found in cart.");
                });

        cart.removeOrderItem(itemToRemove);
        orderItemRepository.delete(itemToRemove); // Ensure the item is also deleted from its repository
        recalculateCartTotal(cart);
        logger.info("Product {} removed from cart for user {}. New cart total: {}", productId, userId, cart.getTotalAmount());
        return orderRepository.save(cart);
    }

    public Optional<Order> getCart(Long userId) {
        logger.info("Fetching cart for user {}", userId);
        Optional<Order> cart = orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING);
        if (cart.isPresent()) {
            logger.info("Cart found for user {}. Total items: {}", userId, cart.get().getOrderItems().size());
        } else {
            logger.info("No active cart found for user {}", userId);
        }
        return cart;
    }

    // --- Order Placement and History ---

    @Transactional
    public Order checkoutCart(Long userId) {
        logger.info("Initiating checkout for user {}", userId);
        Order cart = orderRepository.findByUserIdAndStatus(userId, OrderStatus.PENDING)
                .orElseThrow(() -> {
                    logger.warn("No pending cart found for user {} to checkout.", userId);
                    return new RuntimeException("No pending cart to checkout.");
                });

        if (cart.getOrderItems().isEmpty()) {
            logger.warn("User {}'s cart is empty. Cannot checkout an empty cart.", userId);
            throw new RuntimeException("Cannot checkout an empty cart.");
        }

        // TODO: In a real application, here you would:
        // 1. Call Product Service to decrement stock for each OrderItem
        // 2. Handle payment processing (e.g., call a Payment Service)
        // 3. Create shipping information (e.g., call a Shipping Service)

        cart.setStatus(OrderStatus.PLACED);
        Order placedOrder = orderRepository.save(cart);
        logger.info("Cart for user {} checked out successfully. New Order ID: {}", userId, placedOrder.getId());
        return placedOrder;
    }

    public Optional<Order> getOrderById(Long orderId, Long userId) {
        logger.info("Fetching order {} for user {}", orderId, userId);
        // Ensure user can only retrieve their own orders
        Optional<Order> order = orderRepository.findById(orderId);
        if (order.isPresent() && order.get().getUserId().equals(userId)) {
            logger.info("Order {} found for user {}", orderId, userId);
            return order;
        }
        logger.warn("Order {} not found or does not belong to user {}.", orderId, userId);
        return Optional.empty();
    }

    public List<Order> getOrdersByUserId(Long userId) {
        logger.info("Fetching all orders for user {}", userId);
        List<Order> orders = orderRepository.findByUserId(userId);
        logger.info("Found {} orders for user {}", orders.size(), userId);
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
    // In a real scenario, this would be a RestTemplate/WebClient call
    private BigDecimal getProductPriceFromProductService(Long productId) {
        logger.debug("Simulating call to Product Service for product ID: {}", productId);
        // This is a placeholder. You'd replace this with an actual API call.
        // Example: Assume product 1 costs 10.00, product 2 costs 25.50
        if (productId.equals(1L)) {
            return new BigDecimal("10.00");
        } else if (productId.equals(2L)) {
            return new BigDecimal("25.50");
        } else if (productId.equals(3L)) {
            return new BigDecimal("5.00");
        }
        return null; // Product not found
    }
}
