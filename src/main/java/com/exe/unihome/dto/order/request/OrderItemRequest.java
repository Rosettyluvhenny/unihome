package com.exe.unihome.dto.order.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderItemRequest {

    @NotNull
    private UUID furnitureId;

    @NotNull
    @Min(1)
    private Integer quantity;
}
