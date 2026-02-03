package com.gateway.config;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayConfig {

    /**
     * Configure routes for all microservices
     */
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                // User Service Routes
                .route("user-service", r -> r
                        .path("/api/v1/users/**", "/api/v1/auth/**")
                        .uri("http://localhost:8081"))

                // Bill Service Routes
                .route("bill-service", r -> r
                        .path("/api/v1/bills/**")
                        .uri("http://localhost:8082"))

                // Payment Service Routes
                .route("payment-service", r -> r
                        .path("/api/v1/payments/**")
                        .uri("http://localhost:8083"))

                // Settlement Service Routes
                .route("settlement-service", r -> r
                        .path("/api/v1/settlements/**")
                        .uri("http://localhost:8084"))

                .build();
    }
}