package com.exe.unihome.controller;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.common.exception.GlobalExceptionHandler;
import com.exe.unihome.dto.order.response.OrderResponse;
import com.exe.unihome.persistence.enums.OrderStatus;
import com.exe.unihome.service.OrderService;
import com.exe.unihome.service.ShipmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock private OrderService orderService;
    @Mock private ShipmentService shipmentService;
    @InjectMocks private OrderController orderController;

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private Authentication adminAuth() {
        return new UsernamePasswordAuthenticationToken(
                "admin-1", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    }

    private Authentication staffAuth() {
        return new UsernamePasswordAuthenticationToken(
                "staff-1", null, List.of(new SimpleGrantedAuthority("ROLE_STAFF")));
    }

    private Authentication shipperAuth(String name) {
        return new UsernamePasswordAuthenticationToken(
                name, null, List.of(new SimpleGrantedAuthority("ROLE_SHIPPER")));
    }

    private Authentication customerAuth() {
        return new UsernamePasswordAuthenticationToken(
                "user-123", null, List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
    }

    private OrderResponse buildResponse(UUID orderId, OrderStatus status) {
        return OrderResponse.builder()
                .orderId(orderId)
                .userId("user-123")
                .status(status)
                .subtotal(new BigDecimal("900000"))
                .shippingFee(new BigDecimal("30000"))
                .totalPrice(new BigDecimal("930000"))
                .build();
    }

    // ========== UPDATE ORDER STATUS ==========

    @Nested
    @DisplayName("PATCH /orders/{orderId}/status")
    class UpdateStatus {

        @Test
        @DisplayName("Admin can update order status")
        void admin_canUpdateStatus() throws Exception {
            UUID orderId = UUID.randomUUID();
            OrderResponse response = buildResponse(orderId, OrderStatus.CONFIRMED);

            when(orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED)).thenReturn(response);

            mockMvc.perform(patch("/orders/{orderId}/status", orderId)
                            .principal(adminAuth())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"CONFIRMED\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.code").value(0))
                    .andExpect(jsonPath("$.data.status").value("CONFIRMED"));
        }

        @Test
        @DisplayName("Staff can update order status")
        void staff_canUpdateStatus() throws Exception {
            UUID orderId = UUID.randomUUID();
            OrderResponse response = buildResponse(orderId, OrderStatus.SHIPPING);

            when(orderService.updateOrderStatus(orderId, OrderStatus.SHIPPING)).thenReturn(response);

            mockMvc.perform(patch("/orders/{orderId}/status", orderId)
                            .principal(staffAuth())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"SHIPPING\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("SHIPPING"));
        }

        @Test
        @DisplayName("Shipper assigned to order can update status")
        void shipper_assigned_canUpdateStatus() throws Exception {
            UUID orderId = UUID.randomUUID();
            OrderResponse response = buildResponse(orderId, OrderStatus.COMPLETED);

            when(shipmentService.isShipperAssignedToOrder(orderId, "shipper-1")).thenReturn(true);
            when(orderService.updateOrderStatus(orderId, OrderStatus.COMPLETED)).thenReturn(response);

            mockMvc.perform(patch("/orders/{orderId}/status", orderId)
                            .principal(shipperAuth("shipper-1"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"COMPLETED\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }

        @Test
        @DisplayName("Shipper NOT assigned to order gets 403 with code 8003")
        void shipper_notAssigned_forbidden() throws Exception {
            UUID orderId = UUID.randomUUID();

            when(shipmentService.isShipperAssignedToOrder(orderId, "shipper-999")).thenReturn(false);

            mockMvc.perform(patch("/orders/{orderId}/status", orderId)
                            .principal(shipperAuth("shipper-999"))
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"COMPLETED\"}"))
                    .andExpect(jsonPath("$.code").value(8003));
        }

        @Test
        @DisplayName("Invalid transition returns error code 4002")
        void invalidTransition_returns400() throws Exception {
            UUID orderId = UUID.randomUUID();

            when(orderService.updateOrderStatus(orderId, OrderStatus.COMPLETED))
                    .thenThrow(new AppException(ErrorCode.ORDER_STATUS_TRANSITION_INVALID));

            mockMvc.perform(patch("/orders/{orderId}/status", orderId)
                            .principal(adminAuth())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"status\": \"COMPLETED\"}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.code").value(4002));
        }
    }

    // ========== DELETE (CANCEL) ORDER ==========

    @Nested
    @DisplayName("DELETE /orders/{orderId}")
    class CancelOrderEndpoint {

        @Test
        @DisplayName("Admin can cancel order")
        void admin_canCancel() throws Exception {
            UUID orderId = UUID.randomUUID();
            OrderResponse response = buildResponse(orderId, OrderStatus.CANCELLED);

            when(orderService.cancelOrder(orderId)).thenReturn(response);

            mockMvc.perform(delete("/orders/{orderId}", orderId)
                            .principal(adminAuth()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("CANCELLED"));
        }

        @Test
        @DisplayName("Cancel non-existent order returns error")
        void cancelNonExistent_returnsError() throws Exception {
            UUID orderId = UUID.randomUUID();

            when(orderService.cancelOrder(orderId))
                    .thenThrow(new AppException(ErrorCode.ORDER_NOT_FOUND));

            mockMvc.perform(delete("/orders/{orderId}", orderId)
                            .principal(adminAuth()))
                    .andExpect(jsonPath("$.code").value(ErrorCode.ORDER_NOT_FOUND.getCode()));
        }
    }

    // ========== GET ORDER ==========

    @Nested
    @DisplayName("GET /orders/{orderId}")
    class GetOrder {

        @Test
        @DisplayName("Customer can get own order")
        void customer_canGetOwnOrder() throws Exception {
            UUID orderId = UUID.randomUUID();
            OrderResponse response = buildResponse(orderId, OrderStatus.PENDING);

            when(orderService.getOrder(eq(orderId), eq("user-123"), eq(false))).thenReturn(response);

            mockMvc.perform(get("/orders/{orderId}", orderId)
                            .principal(customerAuth()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.orderId").value(orderId.toString()))
                    .andExpect(jsonPath("$.data.status").value("PENDING"));
        }

        @Test
        @DisplayName("Admin can get any order")
        void admin_canGetAnyOrder() throws Exception {
            UUID orderId = UUID.randomUUID();
            OrderResponse response = buildResponse(orderId, OrderStatus.SHIPPING);

            when(orderService.getOrder(eq(orderId), eq("admin-1"), eq(true))).thenReturn(response);

            mockMvc.perform(get("/orders/{orderId}", orderId)
                            .principal(adminAuth()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("SHIPPING"));
        }
    }
}
