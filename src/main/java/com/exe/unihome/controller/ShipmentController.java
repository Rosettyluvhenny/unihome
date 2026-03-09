package com.exe.unihome.controller;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.dto.shipment.request.CreateShipmentRequest;
import com.exe.unihome.dto.shipment.request.UpdateShipmentStatusRequest;
import com.exe.unihome.dto.shipment.response.ShipmentResponse;
import com.exe.unihome.dto.shipment.response.ShipperCodRevenueResponse;
import com.exe.unihome.dto.shipment.response.ShipperWorkloadResponse;
import com.exe.unihome.persistence.enums.ShipmentStatus;
import com.exe.unihome.service.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/shipments")
@RequiredArgsConstructor
@Tag(name = "Shipments", description = "Shipment management APIs")
@SecurityRequirement(name = "bearerAuth")
public class ShipmentController {

    private final ShipmentService shipmentService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Create shipment and assign shipper")
    public ResponseEntity<ApiResponse<ShipmentResponse>> createShipment(
            @Valid @RequestBody CreateShipmentRequest request) {
        ShipmentResponse response = shipmentService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<ShipmentResponse>builder()
                        .code(0)
                        .message("Shipment created")
                        .data(response)
                        .build());
    }

    @PatchMapping("/{shipmentId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('SHIPPER')")
    @Operation(summary = "Update shipment status")
    public ResponseEntity<ApiResponse<ShipmentResponse>> updateShipmentStatus(
            Authentication authentication,
            @PathVariable String shipmentId,
            @Valid @RequestBody UpdateShipmentStatusRequest request) {
        if (hasRole(authentication, "SHIPPER")
                && !shipmentService.isShipperAssignedToShipment(shipmentId, authentication.getName())) {
            throw new AppException(ErrorCode.SHIPPER_NOT_ASSIGNED);
        }
        ShipmentResponse response = shipmentService.updateShipmentStatus(
                shipmentId, request.getStatus(), request.getNote());
        return ResponseEntity.ok(ApiResponse.<ShipmentResponse>builder()
                .code(0)
                .message("Shipment status updated")
                .data(response)
                .build());
    }

    @GetMapping("/{shipmentId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('SHIPPER')")
    @Operation(summary = "Get shipment details")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getShipment(
            @PathVariable String shipmentId) {
        ShipmentResponse response = shipmentService.getShipment(shipmentId);
        return ResponseEntity.ok(ApiResponse.<ShipmentResponse>builder()
                .code(0)
                .message("Success")
                .data(response)
                .build());
    }

    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('SHIPPER')")
    @Operation(summary = "Get shipment by order ID")
    public ResponseEntity<ApiResponse<ShipmentResponse>> getShipmentByOrderId(
            @PathVariable UUID orderId) {
        ShipmentResponse response = shipmentService.getShipmentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.<ShipmentResponse>builder()
                .code(0)
                .message("Success")
                .data(response)
                .build());
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('SHIPPER')")
    @Operation(summary = "Get shipments assigned to current shipper")
    public ResponseEntity<ApiResponse<Page<ShipmentResponse>>> getMyShipments(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ShipmentStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ShipmentResponse> response = shipmentService.getShipmentsByShipper(
                authentication.getName(), status, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<ShipmentResponse>>builder()
                .code(0)
                .message("Success")
                .data(response)
                .build());
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Admin: Get all shipments with optional filters")
    public ResponseEntity<ApiResponse<Page<ShipmentResponse>>> getAllShipments(
            @RequestParam(required = false) String shipperId,
            @RequestParam(required = false) ShipmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ShipmentResponse> response = shipmentService.getAllShipments(shipperId, status, date, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<ShipmentResponse>>builder()
                .code(0)
                .message("Success")
                .data(response)
                .build());
    }

    @GetMapping("/admin/shippers")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Admin: Get all shippers with workload info")
    public ResponseEntity<ApiResponse<Page<ShipperWorkloadResponse>>> getShippers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fullName").ascending());
        Page<ShipperWorkloadResponse> response = shipmentService.getShippers(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<ShipperWorkloadResponse>>builder()
                .code(0)
                .message("Success")
                .data(response)
                .build());
    }

    @GetMapping("/admin/cod-revenue")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Admin: Get COD cash collected by shipper(s) on a date (default: today)")
    public ResponseEntity<ApiResponse<List<ShipperCodRevenueResponse>>> getCodRevenue(
            @RequestParam(required = false) String shipperId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        List<ShipperCodRevenueResponse> response = shipmentService.getCodRevenue(shipperId, date);
        return ResponseEntity.ok(ApiResponse.<List<ShipperCodRevenueResponse>>builder()
                .code(0)
                .message("Success")
                .data(response)
                .build());
    }

    private boolean hasRole(Authentication authentication, String role) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}
