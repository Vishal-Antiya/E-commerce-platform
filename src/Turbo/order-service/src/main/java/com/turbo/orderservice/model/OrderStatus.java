package com.turbo.orderservice.model;

public enum OrderStatus {
    PENDING, // For items in the cart, not yet checked out
    PLACED,
    SHIPPED,
    DELIVERED,
    CANCELLED
}
