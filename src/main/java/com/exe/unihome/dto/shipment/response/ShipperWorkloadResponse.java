package com.exe.unihome.dto.shipment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShipperWorkloadResponse {

    private String id;
    private String fullName;
    private String phone;
    private String image;
    private long totalShipments;
    private long activeShipments; // PENDING + SHIPPING
}
