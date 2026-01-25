package com.exe.unihome.service;

import com.exe.unihome.dto.order.request.CreateOrderRequest;
import com.exe.unihome.dto.order.response.OrderResponse;
import com.exe.unihome.persistence.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    OrderResponse placeOrder(String userId, CreateOrderRequest request);

    OrderResponse getOrder(UUID orderId, String requesterId, boolean isAdmin);

    Page<OrderResponse> getOrdersForUser(String userId, OrderStatus status, Pageable pageable);

    Page<OrderResponse> getOrdersForAdmin(String userId, OrderStatus status, Pageable pageable);

    OrderResponse updateOrderStatus(UUID orderId, OrderStatus status);

    OrderResponse cancelOrder(UUID orderId);
}
