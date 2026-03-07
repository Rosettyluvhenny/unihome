package com.exe.unihome.dto.shipment.response;

import com.exe.unihome.persistence.enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {

    private String id;
    private String transactionId;
    private String orderId;
    private String shipperId;
    private String shipperName;
    private ShipmentStatus status;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
