package com.exe.unihome.service;

import com.exe.unihome.dto.shipment.request.CreateShipmentRequest;
import com.exe.unihome.dto.shipment.response.ShipmentResponse;
import com.exe.unihome.persistence.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ShipmentService {

    ShipmentResponse createShipment(CreateShipmentRequest request);

    ShipmentResponse updateShipmentStatus(String shipmentId, ShipmentStatus status, String note);

    ShipmentResponse getShipment(String shipmentId);

    ShipmentResponse getShipmentByOrderId(UUID orderId);

    Page<ShipmentResponse> getShipmentsByShipper(String shipperId, ShipmentStatus status, Pageable pageable);

    boolean isShipperAssignedToOrder(UUID orderId, String shipperId);
}
