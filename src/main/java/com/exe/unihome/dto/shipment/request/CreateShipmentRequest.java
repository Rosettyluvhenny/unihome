package com.exe.unihome.dto.shipment.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateShipmentRequest {

    @NotBlank(message = "Transaction ID is required")
    private String transactionId;

    @NotBlank(message = "Shipper ID is required")
    private String shipperId;

    private String note;
}
