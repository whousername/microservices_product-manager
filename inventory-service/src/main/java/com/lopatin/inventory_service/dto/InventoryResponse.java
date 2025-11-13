package com.lopatin.inventory_service.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private String skuCode;
    private boolean isInStock;
}
