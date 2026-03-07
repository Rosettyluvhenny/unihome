package com.exe.unihome.controller;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.dto.order.request.CreateOrderRequest;
import com.exe.unihome.dto.order.request.UpdateOrderStatusRequest;
import com.exe.unihome.dto.order.response.OrderResponse;
import com.exe.unihome.persistence.enums.OrderStatus;
import com.exe.unihome.service.OrderService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
@Tag(name = "Orders", description = "Order management APIs")
@SecurityRequirement(name = "bearerAuth")
public class OrderController {

    private final OrderService orderService;
    private final ShipmentService shipmentService;

    @PostMapping("/orders")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Place an order with custom items and shipping info")
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            Authentication authentication,
            @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse response = orderService.placeOrder(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.<OrderResponse>builder()
                .code(0)
                .message("Order created")
                .data(response)
                .build());
    }

    @GetMapping("/orders/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "List authenticated user's orders")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderResponse> response = orderService.getOrdersForUser(authentication.getName(), status, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<OrderResponse>>builder()
            .code(0)
            .message("Success")
            .data(response)
            .build());
    }

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
    @Operation(summary = "Get order details")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
            Authentication authentication,
            @PathVariable UUID orderId) {
        boolean isAdmin = isAdmin(authentication);
        OrderResponse response = orderService.getOrder(orderId, authentication.getName(), isAdmin);
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
            .code(0)
            .message("Success")
            .data(response)
            .build());
    }

    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Search orders (admin)")
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getOrdersForAdmin(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) String userId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<OrderResponse> response = orderService.getOrdersForAdmin(userId, status, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<OrderResponse>>builder()
            .code(0)
            .message("Success")
            .data(response)
            .build());
    }

    @PatchMapping("/orders/{orderId}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF') or hasRole('SHIPPER')")
    @Operation(summary = "Update order status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            Authentication authentication,
            @PathVariable UUID orderId,
            @Valid @RequestBody UpdateOrderStatusRequest request) {
        if (isShipper(authentication)
                && !shipmentService.isShipperAssignedToOrder(orderId, authentication.getName())) {
            throw new AppException(ErrorCode.SHIPPER_NOT_ASSIGNED);
        }
        OrderResponse response = orderService.updateOrderStatus(orderId, request.getStatus());
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
            .code(0)
            .message("Status updated")
            .data(response)
            .build());
    }

    @DeleteMapping("/orders/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cancel order and restock")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable UUID orderId) {
        OrderResponse response = orderService.cancelOrder(orderId);
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
            .code(0)
            .message("Order cancelled")
            .data(response)
            .build());
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(authority -> authority.equals("ROLE_ADMIN"));
    }

    private boolean isShipper(Authentication authentication) {
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .anyMatch(authority -> authority.equals("ROLE_SHIPPER"));
    }
}
