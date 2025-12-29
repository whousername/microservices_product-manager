package com.lopatin.order_service.dto;

import com.lopatin.order_service.model.UserDetails;
import java.util.List;

public record OrderRequest(
        List<OrderLineItemsDto> orderLineItemsDtoList,
        UserDetails userDetails
) {
    public List<OrderLineItemsDto> getOrderLineItemsDtoList(){
        return orderLineItemsDtoList();
    }
}
