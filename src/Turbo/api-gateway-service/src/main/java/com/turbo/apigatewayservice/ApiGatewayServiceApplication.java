package com.turbo.apigatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableDiscoveryClient
@EnableWebFluxSecurity
public class ApiGatewayServiceApplication {

    private static final Logger logger = LoggerFactory.getLogger(ApiGatewayServiceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayServiceApplication.class, args);
        logger.info("API Gateway Service started successfully!");
    }

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        logger.info("Configuring API Gateway routes...");
        return builder.routes()
                .route("user-service-route", r -> r
                        .path("/api/user/**", "/api/auth/**")
                        .uri("lb://user-service")
                )
                .route("admin-service-route", r -> r
                        .path("/api/admin/**")
                        .uri("lb://admin-service")
                )
                .route("product-service-route", r -> r
                        .path("/api/products/**")
                        .uri("lb://product-service")
                )
                .route("order-service-route", r -> r
                        .path("/api/orders/**")
                        .uri("lb://order-service")
                )
                .build();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable()); // Disable CSRF for JWT (reactive)
        logger.info("Configuring API Gateway Security: CSRF disabled.");
        return http.build();
    }
}