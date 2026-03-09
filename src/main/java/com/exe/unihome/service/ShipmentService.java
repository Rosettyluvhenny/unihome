package com.exe.unihome.service;

import com.exe.unihome.dto.shipment.request.CreateShipmentRequest;
import com.exe.unihome.dto.shipment.response.ShipmentResponse;
import com.exe.unihome.dto.shipment.response.ShipperCodRevenueResponse;
import com.exe.unihome.dto.shipment.response.ShipperWorkloadResponse;
import com.exe.unihome.persistence.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ShipmentService {

    ShipmentResponse createShipment(CreateShipmentRequest request);

    ShipmentResponse updateShipmentStatus(String shipmentId, ShipmentStatus status, String note);

    ShipmentResponse getShipment(String shipmentId);

    ShipmentResponse getShipmentByOrderId(UUID orderId);

    Page<ShipmentResponse> getShipmentsByShipper(String shipperId, ShipmentStatus status, Pageable pageable);

    boolean isShipperAssignedToOrder(UUID orderId, String shipperId);

    boolean isShipperAssignedToShipment(String shipmentId, String shipperId);

    Page<ShipmentResponse> getAllShipments(String shipperId, ShipmentStatus status, LocalDate date, Pageable pageable);

    Page<ShipperWorkloadResponse> getShippers(Pageable pageable);

    List<ShipperCodRevenueResponse> getCodRevenue(String shipperId, LocalDate date);
}
