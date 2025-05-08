package com.turbo.apigatewayservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity; // Use reactive version
import org.springframework.security.config.web.server.ServerHttpSecurity; // Use reactive version
import org.springframework.security.web.server.SecurityWebFilterChain;  // Use reactive version
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository; //reactive
//import org.springframework.security.web.server.csrf.CsrfConfigurer; //reactive

@SpringBootApplication
@EnableDiscoveryClient
@EnableWebFluxSecurity // Use this for WebFlux
public class ApiGatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayServiceApplication.class, args);
    }

    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("user-service-route", r -> r
                        .path("/api/auth/**", "/api/users/**")
                        .uri("lb://user-service")
                )
                .build();
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable());; // Disable CSRF for JWT (reactive)
        return http.build();
    }
}
