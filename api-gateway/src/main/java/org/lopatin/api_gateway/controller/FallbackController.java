package org.lopatin.api_gateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class FallbackController {


    @RequestMapping("/fallback/product")
    public Mono<String> fallbackProductService(){
        return Mono.just("Product service is temporary unavailable. Try again later.");
    }

    @RequestMapping("/fallback/order")
    public Mono<String> fallbackOrderService(){
        return Mono.just("Order service is temporary unavailable. Try again later.");
    }
}
