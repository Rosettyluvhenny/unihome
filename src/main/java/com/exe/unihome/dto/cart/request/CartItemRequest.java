package com.exe.unihome.dto.cart.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
public class CartItemRequest {

    @NotNull(message = "Furniture ID is required")
    private UUID furnitureId;

    @NotNull(message = "SKU ID is required")
    private UUID skuId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be greater than zero")
    private Integer quantity;
}
