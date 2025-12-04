package org.lopatin.api_gateway.routes;


import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Routes {

    @Bean
    public RouteLocator routeLocator (RouteLocatorBuilder builder){
        return builder.routes()

                .route("product-service", r -> r.path("/api/product/**")
                        .uri("lb://product-service"))

                .route("order-service", r -> r.path("/api/order/**")
                        .uri("lb://order-service"))

                .route("discovery-static-resources", r -> r
                        .path("/eureka/web")
                        .filters(f -> f.setPath("/"))
                        .uri("http://localhost:8761"))

                .route("discovery-server", r -> r.path("/eureka/**")
                        .uri("http://localhost:8761"))


                // Product Service OpenAPI
                .route("product-service-swagger", r -> r
                        .path("/aggregate/product-service/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://product-service"))

                // Order Service OpenAPI
                .route("order-service-swagger", r -> r
                        .path("/aggregate/order-service/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://order-service"))

                // Inventory Service OpenAPI
                .route("inventory-service-swagger", r -> r
                        .path("/aggregate/inventory-service/v3/api-docs")
                        .filters(f -> f.setPath("/v3/api-docs"))
                        .uri("lb://inventory-service"))


                .build();
    }

}
