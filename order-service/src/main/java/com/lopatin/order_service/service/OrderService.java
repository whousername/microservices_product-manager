package com.lopatin.order_service.service;

import com.lopatin.order_service.dto.InventoryResponse;
import com.lopatin.order_service.dto.OrderLineItemsDto;
import com.lopatin.order_service.dto.OrderRequest;
import com.lopatin.order_service.event.OrderPlacedEvent;
import com.lopatin.order_service.model.Order;
import com.lopatin.order_service.model.OrderLineItems;
import com.lopatin.order_service.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.ReactiveCircuitBreakerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j

public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    private final ReactiveCircuitBreakerFactory<?, ?> rCircuitBreakerFactory;
    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;


    public void placeOrder(OrderRequest orderRequest, String userToken){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());

        List<OrderLineItems> orderLineItemsList = orderRequest.getOrderLineItemsDtoList().stream()
                .map(this::fromDtoToModel)
                .toList();
        order.setOrderLineItems(orderLineItemsList);

        List<String> skuCodes = order.getOrderLineItems().stream()
                .map(OrderLineItems::getSkuCode)
                .toList();


        InventoryResponse[] inventoryResponseArray = rCircuitBreakerFactory
                .create("inventory")
                .run(
                webClientBuilder.build().get()
                        .uri("http://inventory-service/api/inventory",
                                uriBuilder -> uriBuilder.queryParam("skuCodes", skuCodes).build())
                        .headers(headers -> headers.setBearerAuth(userToken))
                        .retrieve()
                        .bodyToMono(InventoryResponse[].class),throwable -> Mono.error(
                                new IllegalStateException("Inventory service unavailable. Try again later.")
                        )
                )
                .block();

        if (inventoryResponseArray == null || inventoryResponseArray.length == 0) {
            throw new IllegalArgumentException("Inventory service returned empty response");
        }

        boolean isInStock = Arrays.stream(inventoryResponseArray)
                .allMatch(InventoryResponse::isInStock);

        if (!isInStock) {
            throw new IllegalArgumentException("One or more products are NOT in stock");
        }

        var savedOrder = orderRepository.save(order);

        var orderPlacedEvent = new OrderPlacedEvent(order.getOrderNumber(), orderRequest.userDetails()
                .email(),
                orderRequest.userDetails()
                        .firstName(),
                orderRequest.userDetails()
                        .lastName());

        if(orderRepository.existsById(savedOrder.getId())){
            log.info("Start - sending OrderPlacedEvent {} to Kafka topic", orderPlacedEvent);
            kafkaTemplate.send("order-placed", orderPlacedEvent);
            log.info("End - sending OrderPlacedEvent {} to Kafka topic", orderPlacedEvent);
        }

    }

    private OrderLineItems fromDtoToModel(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;
    }


}
