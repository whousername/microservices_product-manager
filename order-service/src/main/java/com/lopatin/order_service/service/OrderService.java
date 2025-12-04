package com.lopatin.order_service.service;

import com.lopatin.order_service.dto.InventoryResponse;
import com.lopatin.order_service.dto.OrderLineItemsDto;
import com.lopatin.order_service.dto.OrderRequest;
import com.lopatin.order_service.model.Order;
import com.lopatin.order_service.model.OrderLineItems;
import com.lopatin.order_service.repository.OrderRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;

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

        InventoryResponse[] inventoryResponseArray = webClientBuilder.build().get()
                        .uri("http://inventory-service/api/inventory",
                                uriBuilder -> uriBuilder.queryParam("skuCodes", skuCodes).build())
                        .headers(headers -> headers.setBearerAuth(userToken))
                        .retrieve()
                        .bodyToMono(InventoryResponse[].class)
                        .block();

        if (inventoryResponseArray == null || inventoryResponseArray.length == 0) {
            throw new IllegalArgumentException("Inventory service returned empty response");
        }

        boolean isInStock = Arrays.stream(inventoryResponseArray)
                .allMatch(InventoryResponse::isInStock);

        if (!isInStock) {
            throw new IllegalArgumentException("One or more products are NOT in stock");
        }

        orderRepository.save(order);

    }

    private OrderLineItems fromDtoToModel(OrderLineItemsDto orderLineItemsDto) {
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        return orderLineItems;
    }


}
