package com.turbo.orderservice.controller;

import com.turbo.orderservice.model.Order;
import com.turbo.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "APIs for managing shopping carts and orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    // Helper to get current authenticated user's ID
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User is not authenticated.");
        }
        // Assuming your UserDetails contains the user's ID, or you can retrieve it from your User model
        // For simplicity, we'll use the username as a stand-in for ID for now, or you'd fetch it from user-service
        // In a real app, you'd likely have a custom UserDetails object holding the actual DB ID.
        // For this example, let's assume the username IS the user ID as a string, or fetch it.
        // A more robust solution would involve a custom UserDetails object that stores the userId.
        // For now, let's mock it or assume simple UIDs.
        // For demo purposes, we'll assume a dummy user ID of 1L for all authenticated users.
        // You would typically extract the user ID from the JWT claims or query the user-service.
        return 1L; // Placeholder: Replace with actual user ID from token/db
    }

    @Operation(summary = "Add product to cart", description = "Adds a specified quantity of a product to the current user's shopping cart. Creates a cart if none exists.")
    @ApiResponse(responseCode = "200", description = "Product added/updated in cart",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class)))
    @ApiResponse(responseCode = "400", description = "Invalid product ID or quantity")
    @ApiResponse(responseCode = "404", description = "Product not found")
    @PostMapping("/cart/add")
    public ResponseEntity<?> addProductToCart(@RequestBody Map<String, Object> request) {
        try {
            Long userId = getCurrentUserId(); // Get ID of the authenticated user
            Long productId = Long.valueOf(request.get("productId").toString());
            int quantity = Integer.parseInt(request.get("quantity").toString());

            logger.info("API Request: Add product {} (quantity {}) to cart for user {}", productId, quantity, userId);
            Order updatedCart = orderService.addProductToCart(userId, productId, quantity);
            return ResponseEntity.ok(updatedCart);
        } catch (NumberFormatException e) {
            logger.error("Invalid product ID or quantity format: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid product ID or quantity format.");
        } catch (RuntimeException e) {
            logger.error("Error adding product to cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Update product quantity in cart", description = "Updates the quantity of a specific product in the current user's cart.")
    @ApiResponse(responseCode = "200", description = "Product quantity updated in cart",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class)))
    @ApiResponse(responseCode = "400", description = "Invalid quantity or product not in cart")
    @ApiResponse(responseCode = "404", description = "Cart or Product not found")
    @PutMapping("/cart/update")
    public ResponseEntity<?> updateProductQuantityInCart(@RequestBody Map<String, Object> request) {
        try {
            Long userId = getCurrentUserId();
            Long productId = Long.valueOf(request.get("productId").toString());
            int quantity = Integer.parseInt(request.get("quantity").toString());

            logger.info("API Request: Update product {} quantity to {} for user {}", productId, quantity, userId);
            Order updatedCart = orderService.updateProductQuantityInCart(userId, productId, quantity);
            return ResponseEntity.ok(updatedCart);
        } catch (NumberFormatException e) {
            logger.error("Invalid product ID or quantity format: {}", e.getMessage());
            return ResponseEntity.badRequest().body("Invalid product ID or quantity format.");
        } catch (RuntimeException e) {
            logger.error("Error updating product quantity in cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Remove product from cart", description = "Removes a product from the current user's cart.")
    @ApiResponse(responseCode = "200", description = "Product removed from cart",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class)))
    @ApiResponse(responseCode = "404", description = "Cart or Product not found in cart")
    @DeleteMapping("/cart/remove/{productId}")
    public ResponseEntity<?> removeProductFromCart(
            @Parameter(description = "ID of the product to remove", required = true)
            @PathVariable Long productId) {
        try {
            Long userId = getCurrentUserId();
            logger.info("API Request: Remove product {} from cart for user {}", productId, userId);
            Order updatedCart = orderService.removeProductFromCart(userId, productId);
            return ResponseEntity.ok(updatedCart);
        } catch (RuntimeException e) {
            logger.error("Error removing product from cart: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @Operation(summary = "Get current user's cart", description = "Retrieves the contents of the current user's active shopping cart.")
    @ApiResponse(responseCode = "200", description = "Cart retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class)))
    @ApiResponse(responseCode = "404", description = "Cart not found")
    @GetMapping("/cart")
    public ResponseEntity<Order> getCart() {
        Long userId = getCurrentUserId();
        logger.info("API Request: Get cart for user {}", userId);
        Optional<Order> cart = orderService.getCart(userId);
        return cart.map(value -> {
            logger.info("Cart retrieved successfully for user {}", userId);
            return new ResponseEntity<>(value, HttpStatus.OK);
        }).orElseGet(() -> {
            logger.warn("Cart not found for user {}", userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        });
    }

    @Operation(summary = "Checkout cart", description = "Converts the current user's pending cart into a placed order.")
    @ApiResponse(responseCode = "200", description = "Cart checked out successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class)))
    @ApiResponse(responseCode = "400", description = "Empty cart or checkout failure")
    @ApiResponse(responseCode = "404", description = "Cart not found")
    @PostMapping("/checkout")
    public ResponseEntity<?> checkoutCart() {
            Long userId = getCurrentUserId(); // Changed
        try {
            logger.info("API Request: Checkout cart for user {}", userId);
            Order placedOrder = orderService.checkoutCart(userId);
            return ResponseEntity.ok(placedOrder);
        } catch (RuntimeException e) {
            logger.error("Error during checkout for user {}: {}", userId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @Operation(summary = "Get order by ID", description = "Retrieves details of a specific order by its ID for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Order retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Order.class)))
    @ApiResponse(responseCode = "404", description = "Order not found or does not belong to user")
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(
            @Parameter(description = "ID of the order to retrieve", required = true)
            @PathVariable Long orderId) {
        Long userId = getCurrentUserId();
        logger.info("API Request: Get order {} for user {}", orderId, userId);
        Optional<Order> order = orderService.getOrderById(orderId, userId);
        return order.map(value -> {
            logger.info("Order {} retrieved successfully for user {}", orderId, userId);
            return new ResponseEntity<>(value, HttpStatus.OK);
        }).orElseGet(() -> {
            logger.warn("Order {} not found or does not belong to user {}", orderId, userId);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        });
    }

    @Operation(summary = "Get all orders for current user", description = "Retrieves a list of all historical orders for the authenticated user.")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully",
            content = @Content(mediaType = "application/json",
                    schema = @Schema(type = "array", implementation = Order.class)))
    @GetMapping("/history")
    public ResponseEntity<List<Order>> getOrdersByUserId() {
        Long userId = getCurrentUserId();
        logger.info("API Request: Get order history for user {}", userId);
        List<Order> orders = orderService.getOrdersByUserId(userId);
        logger.info("Retrieved {} orders for user {}", orders.size(), userId);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }
}
