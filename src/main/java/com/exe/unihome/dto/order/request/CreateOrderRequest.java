package com.exe.unihome.dto.order.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {

    @NotNull
    @Valid
    private ShippingInfoRequest shippingInfo;

    @NotEmpty
    @Valid
    private List<OrderItemRequest> items;
}
