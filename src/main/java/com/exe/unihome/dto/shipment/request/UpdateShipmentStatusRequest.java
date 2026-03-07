package com.exe.unihome.dto.shipment.request;

import com.exe.unihome.persistence.enums.ShipmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateShipmentStatusRequest {

    @NotNull(message = "Status is required")
    private ShipmentStatus status;

    private String note;
}
