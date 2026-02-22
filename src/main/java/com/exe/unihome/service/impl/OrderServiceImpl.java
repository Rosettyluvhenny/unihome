package com.exe.unihome.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.distance.DistanceResponse;
import com.exe.unihome.dto.order.request.CreateOrderRequest;
import com.exe.unihome.dto.order.request.OrderItemRequest;
import com.exe.unihome.dto.order.request.ShippingInfoRequest;
import com.exe.unihome.dto.order.response.OrderResponse;
import com.exe.unihome.mapper.OrderMapper;
import com.exe.unihome.notification.NotificationChannel;
import com.exe.unihome.notification.NotificationType;
import com.exe.unihome.notification.service.NotificationService;
import com.exe.unihome.persistence.entity.Furniture;
import com.exe.unihome.persistence.entity.FurnitureSku;
import com.exe.unihome.persistence.entity.order.Order;
import com.exe.unihome.persistence.entity.order.OrderItem;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import com.exe.unihome.persistence.enums.OrderStatus;
import com.exe.unihome.persistence.repository.FurnitureRepository;
import com.exe.unihome.persistence.repository.FurnitureSkuRepository;
import com.exe.unihome.persistence.repository.OrderRepository;
import com.exe.unihome.persistence.repository.UserRepository;
import com.exe.unihome.service.DistanceService;
import com.exe.unihome.service.OrderService;
import com.exe.unihome.service.ShippingFeeService;
import com.exe.unihome.service.model.ShippingFeeResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        ALLOWED_TRANSITIONS.put(OrderStatus.PENDING, EnumSet.of(OrderStatus.CONFIRMED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.CONFIRMED, EnumSet.of(OrderStatus.SHIPPING, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.SHIPPING, EnumSet.of(OrderStatus.COMPLETED, OrderStatus.CANCELLED));
        ALLOWED_TRANSITIONS.put(OrderStatus.COMPLETED, EnumSet.noneOf(OrderStatus.class));
        ALLOWED_TRANSITIONS.put(OrderStatus.CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    private final OrderRepository orderRepository;
    private final FurnitureRepository furnitureRepository;
    private final FurnitureSkuRepository skuRepository;
    private final UserRepository userRepository;
    private final DistanceService distanceService;
    private final ShippingFeeService shippingFeeService;
    private final OrderMapper orderMapper;
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public OrderResponse placeOrder(String userId, CreateOrderRequest request) {
        validateRequest(request);

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        // Aggregate by skuId (not furnitureId)
        Map<UUID, Integer> quantities = aggregateItems(request.getItems());

        Order order = Order.builder()
            .user(user)
            .status(OrderStatus.PENDING)
            .shippingFullName(request.getShippingInfo().getFullName())
            .shippingPhone(request.getShippingInfo().getPhone())
            .shippingAddress(request.getShippingInfo().getAddress())
            .shippingNote(request.getShippingInfo().getNote())
            .build();

        BigDecimal subtotal = BigDecimal.ZERO;

        for (Map.Entry<UUID, Integer> entry : quantities.entrySet()) {
            UUID skuId = entry.getKey();
            Integer requestedQty = entry.getValue();

            FurnitureSku sku = skuRepository.findBySkuId(skuId)
                .orElseThrow(() -> new AppException(ErrorCode.SKU_NOT_FOUND));

            Furniture furniture = sku.getFurniture();

            if (requestedQty == null || requestedQty <= 0) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }

            if (sku.getStock() < requestedQty) {
                throw new AppException(ErrorCode.SKU_OUT_OF_STOCK);
            }

            // Deduct SKU stock
            sku.setStock(sku.getStock() - requestedQty);
            skuRepository.save(sku);

            // Sync furniture-level stock
            syncFurnitureStock(furniture);

            BigDecimal unitPrice = resolveUnitPrice(sku);
            OrderItem orderItem = OrderItem.builder()
                .order(order)
                .furniture(furniture)
                .sku(sku)
                .quantity(requestedQty)
                .price(unitPrice)
                .build();
            order.getItems().add(orderItem);

            subtotal = subtotal.add(unitPrice.multiply(BigDecimal.valueOf(requestedQty)));
        }

        DistanceResponse distanceResponse = distanceService.getDistanceToWarehouse(userId);
        double distanceKm = distanceResponse.getDistanceKilometers() != null
            ? distanceResponse.getDistanceKilometers()
            : 0d;

        ShippingFeeResult shippingFeeResult = shippingFeeService.calculate(distanceKm, subtotal);
        BigDecimal shippingFee = shippingFeeResult.getShippingFee();
        BigDecimal total = subtotal.add(shippingFee);

        order.setSubtotal(subtotal);
        order.setShippingFee(shippingFee);
        order.setTotalPrice(total);
        order.setFreeShippingApplied(shippingFeeResult.isFreeApplied());
        order.setDistanceKm(BigDecimal.valueOf(shippingFeeResult.getDistanceKm()).setScale(2, RoundingMode.HALF_UP));

        Order savedOrder = orderRepository.save(order);
        return orderMapper.toResponse(savedOrder);
    }

    private void validateRequest(CreateOrderRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        ShippingInfoRequest info = request.getShippingInfo();
        if (info == null) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
    }

    private Map<UUID, Integer> aggregateItems(List<OrderItemRequest> items) {
        Map<UUID, Integer> aggregated = new LinkedHashMap<>();
        for (OrderItemRequest item : items) {
            if (item.getSkuId() == null || item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            aggregated.merge(item.getSkuId(), item.getQuantity(), Integer::sum);
        }
        return aggregated;
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID orderId, String requesterId, boolean isAdmin) {
        Optional<Order> orderOpt = isAdmin
            ? orderRepository.findByOrderId(orderId)
            : orderRepository.findByOrderIdAndUserId(orderId, requesterId);

        Order order = orderOpt.orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return orderMapper.toResponse(order);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersForUser(String userId, OrderStatus status, Pageable pageable) {
        Page<Order> orders = status == null
            ? orderRepository.findByUserId(userId, pageable)
            : orderRepository.findByUserIdAndStatus(userId, status, pageable);
        return orders.map(orderMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersForAdmin(String userId, OrderStatus status, Pageable pageable) {
        Page<Order> orders;
        if (userId != null && status != null) {
            orders = orderRepository.findByUserIdAndStatus(userId, status, pageable);
        } else if (userId != null) {
            orders = orderRepository.findByUserId(userId, pageable);
        } else if (status != null) {
            orders = orderRepository.findByStatus(status, pageable);
        } else {
            orders = orderRepository.findAllWithDetails(pageable);
        }
        return orders.map(orderMapper::toResponse);
    }

    @Override
    @Transactional
    public OrderResponse updateOrderStatus(UUID orderId, OrderStatus status) {
        if (status == null) {
            throw new AppException(ErrorCode.ORDER_STATUS_INVALID);
        }
        Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == status) {
            return orderMapper.toResponse(order);
        }

        if (status == OrderStatus.CANCELLED) {
            return cancelOrderInternal(order);
        }

        if (!isTransitionAllowed(order.getStatus(), status)) {
            throw new AppException(ErrorCode.ORDER_STATUS_TRANSITION_INVALID);
        }

        order.setStatus(status);
        Order saved = orderRepository.save(order);

        // Gửi thông báo cho customer
        sendOrderStatusNotification(saved);

        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public OrderResponse cancelOrder(UUID orderId) {
        Order order = orderRepository.findByOrderId(orderId)
            .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        return cancelOrderInternal(order);
    }

    private OrderResponse cancelOrderInternal(Order order) {
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new AppException(ErrorCode.ORDER_ALREADY_CANCELLED);
        }
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new AppException(ErrorCode.ORDER_STATUS_TRANSITION_INVALID);
        }

        if (order.getItems() != null) {
            order.getItems().forEach(orderItem -> {
                if (orderItem.getSku() != null) {
                    FurnitureSku sku = skuRepository.findBySkuId(orderItem.getSku().getSkuId())
                        .orElseThrow(() -> new AppException(ErrorCode.SKU_NOT_FOUND));
                    sku.setStock(sku.getStock() + orderItem.getQuantity());
                    skuRepository.save(sku);
                    syncFurnitureStock(sku.getFurniture());
                } else {
                    // Fallback for legacy orders without SKU
                    Furniture furniture = furnitureRepository.findById(orderItem.getFurniture().getFurnitureId())
                        .orElseThrow(() -> new AppException(ErrorCode.FURNITURE_NOT_FOUND));
                    furniture.setStock(furniture.getStock() + orderItem.getQuantity());
                    furnitureRepository.save(furniture);
                }
            });
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        // Gửi thông báo cho customer
        sendOrderStatusNotification(saved);

        return orderMapper.toResponse(saved);
    }

    private boolean isTransitionAllowed(OrderStatus current, OrderStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(OrderStatus.class)).contains(target);
    }

    private BigDecimal resolveUnitPrice(FurnitureSku sku) {
        return sku.getFinalPrice() != null ? sku.getFinalPrice() : sku.getPrice();
    }

    private void sendOrderStatusNotification(Order order) {
        try {
            String userId = order.getUser().getId();
            String title = buildOrderStatusMessage(order.getStatus(), order.getOrderId());

            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("orderId", order.getOrderId().toString());
            payload.put("status", order.getStatus().toString());
            payload.put("action", NotificationType.ORDER.toString());

            notificationService.createNotification(
                    userId, title, NotificationType.ORDER, NotificationChannel.WEBSOCKET, payload);
        } catch (Exception e) {
            log.warn("Failed to send order status notification for order {}: {}",
                    order.getOrderId(), e.getMessage());
        }
    }

    private String buildOrderStatusMessage(OrderStatus status, UUID orderId) {
        String shortId = orderId.toString().substring(0, 8).toUpperCase();
        return switch (status) {
            case PENDING    -> "Đơn hàng #" + shortId + " đã được tạo";
            case CONFIRMED  -> "Đơn hàng #" + shortId + " đã được xác nhận";
            case SHIPPING   -> "Đơn hàng #" + shortId + " đang được giao";
            case COMPLETED  -> "Đơn hàng #" + shortId + " đã hoàn thành";
            case CANCELLED  -> "Đơn hàng #" + shortId + " đã bị hủy";
        };
    }

    private void syncFurnitureStock(Furniture furniture) {
        List<FurnitureSku> allSkus = skuRepository.findByFurnitureFurnitureIdAndStatus(
                furniture.getFurnitureId(),
                com.exe.unihome.persistence.enums.FurnitureStatus.AVAILABLE);
        int totalStock = allSkus.stream().mapToInt(FurnitureSku::getStock).sum();
        furniture.setStock(totalStock);
        furnitureRepository.save(furniture);
    }
}
