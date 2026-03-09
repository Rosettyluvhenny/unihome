package com.exe.unihome.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.distance.DistanceResponse;
import com.exe.unihome.dto.order.request.CreateOrderRequest;
import com.exe.unihome.dto.order.request.OrderItemRequest;
import com.exe.unihome.dto.order.request.ShippingInfoRequest;
import com.exe.unihome.dto.order.response.OrderResponse;
import com.exe.unihome.mapper.OrderMapper;
import com.exe.unihome.notification.service.NotificationService;
import com.exe.unihome.persistence.entity.Furniture;
import com.exe.unihome.persistence.entity.FurnitureSku;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import com.exe.unihome.persistence.entity.order.Order;
import com.exe.unihome.persistence.entity.order.OrderItem;
import com.exe.unihome.persistence.enums.FurnitureStatus;
import com.exe.unihome.persistence.enums.OrderStatus;
import com.exe.unihome.persistence.repository.FurnitureRepository;
import com.exe.unihome.persistence.repository.FurnitureSkuRepository;
import com.exe.unihome.persistence.repository.OrderRepository;
import com.exe.unihome.persistence.repository.UserRepository;
import com.exe.unihome.service.DistanceService;
import com.exe.unihome.service.ShippingFeeService;
import com.exe.unihome.service.model.ShippingFeeResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private FurnitureRepository furnitureRepository;
    @Mock private FurnitureSkuRepository skuRepository;
    @Mock private UserRepository userRepository;
    @Mock private DistanceService distanceService;
    @Mock private ShippingFeeService shippingFeeService;
    @Mock private OrderMapper orderMapper;
    @Mock private NotificationService notificationService;
    @Spy  private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OrderServiceImpl orderService;

    private User testUser;
    private Furniture testFurniture;
    private FurnitureSku testSku;
    private UUID skuId;
    private UUID furnitureId;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = "user-123";
        skuId = UUID.randomUUID();
        furnitureId = UUID.randomUUID();

        testUser = User.builder().id(userId).fullName("Test User").email("test@test.com").build();

        testFurniture = Furniture.builder()
                .furnitureId(furnitureId)
                .name("Test Chair")
                .price(new BigDecimal("500000"))
                .stock(10)
                .status(FurnitureStatus.AVAILABLE)
                .build();

        testSku = FurnitureSku.builder()
                .skuId(skuId)
                .skuCode("CHAIR-RED")
                .furniture(testFurniture)
                .price(new BigDecimal("500000"))
                .finalPrice(new BigDecimal("450000"))
                .stock(5)
                .status(FurnitureStatus.AVAILABLE)
                .build();
    }

    // ========== PLACE ORDER ==========

    @Nested
    @DisplayName("placeOrder")
    class PlaceOrder {

        private CreateOrderRequest buildValidRequest() {
            OrderItemRequest item = new OrderItemRequest();
            item.setSkuId(skuId);
            item.setFurnitureId(furnitureId);
            item.setQuantity(2);

            ShippingInfoRequest shipping = new ShippingInfoRequest();
            shipping.setFullName("Nguyen Van A");
            shipping.setPhone("0901234567");
            shipping.setAddress("123 ABC, HCM");

            CreateOrderRequest request = new CreateOrderRequest();
            request.setItems(List.of(item));
            request.setShippingInfo(shipping);
            return request;
        }

        @Test
        @DisplayName("Should place order successfully")
        void placeOrder_success() {
            CreateOrderRequest request = buildValidRequest();
            OrderResponse expectedResponse = OrderResponse.builder()
                    .orderId(UUID.randomUUID())
                    .status(OrderStatus.PENDING)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(skuRepository.findBySkuId(skuId)).thenReturn(Optional.of(testSku));
            when(skuRepository.findByFurnitureFurnitureIdAndStatus(furnitureId, FurnitureStatus.AVAILABLE))
                    .thenReturn(List.of(testSku));
            when(distanceService.getDistanceToWarehouse(userId))
                    .thenReturn(DistanceResponse.builder().distanceKilometers(5.0).build());
            when(shippingFeeService.calculate(eq(5.0), any(BigDecimal.class)))
                    .thenReturn(ShippingFeeResult.builder()
                            .shippingFee(new BigDecimal("30000"))
                            .freeApplied(false)
                            .distanceKm(5.0)
                            .build());
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                o.setOrderId(UUID.randomUUID());
                return o;
            });
            when(orderMapper.toResponse(any(Order.class))).thenReturn(expectedResponse);

            OrderResponse result = orderService.placeOrder(userId, request);

            assertNotNull(result);
            assertEquals(OrderStatus.PENDING, result.getStatus());

            // Verify stock was deducted
            ArgumentCaptor<FurnitureSku> skuCaptor = ArgumentCaptor.forClass(FurnitureSku.class);
            verify(skuRepository).save(skuCaptor.capture());
            assertEquals(3, skuCaptor.getValue().getStock()); // 5 - 2 = 3
        }

        @Test
        @DisplayName("Should calculate correct totals")
        void placeOrder_correctTotals() {
            CreateOrderRequest request = buildValidRequest();

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(skuRepository.findBySkuId(skuId)).thenReturn(Optional.of(testSku));
            when(skuRepository.findByFurnitureFurnitureIdAndStatus(furnitureId, FurnitureStatus.AVAILABLE))
                    .thenReturn(List.of(testSku));
            when(distanceService.getDistanceToWarehouse(userId))
                    .thenReturn(DistanceResponse.builder().distanceKilometers(3.0).build());
            when(shippingFeeService.calculate(eq(3.0), any(BigDecimal.class)))
                    .thenReturn(ShippingFeeResult.builder()
                            .shippingFee(new BigDecimal("20000"))
                            .freeApplied(false)
                            .distanceKm(3.0)
                            .build());
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(OrderResponse.builder().build());

            orderService.placeOrder(userId, request);

            ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(orderCaptor.capture());
            Order savedOrder = orderCaptor.getValue();

            // finalPrice = 450000, qty = 2 → subtotal = 900000
            assertEquals(0, new BigDecimal("900000").compareTo(savedOrder.getSubtotal()));
            assertEquals(0, new BigDecimal("20000").compareTo(savedOrder.getShippingFee()));
            assertEquals(0, new BigDecimal("920000").compareTo(savedOrder.getTotalPrice()));
        }

        @Test
        @DisplayName("Should throw when request is null")
        void placeOrder_nullRequest() {
            AppException ex = assertThrows(AppException.class,
                    () -> orderService.placeOrder(userId, null));
            assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        }

        @Test
        @DisplayName("Should throw when items list is empty")
        void placeOrder_emptyItems() {
            CreateOrderRequest request = new CreateOrderRequest();
            request.setItems(Collections.emptyList());
            request.setShippingInfo(new ShippingInfoRequest());

            AppException ex = assertThrows(AppException.class,
                    () -> orderService.placeOrder(userId, request));
            assertEquals(ErrorCode.INVALID_REQUEST, ex.getErrorCode());
        }

        @Test
        @DisplayName("Should throw when user not found")
        void placeOrder_userNotFound() {
            CreateOrderRequest request = buildValidRequest();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            AppException ex = assertThrows(AppException.class,
                    () -> orderService.placeOrder(userId, request));
            assertEquals(ErrorCode.USER_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("Should throw when SKU not found")
        void placeOrder_skuNotFound() {
            CreateOrderRequest request = buildValidRequest();
            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(skuRepository.findBySkuId(skuId)).thenReturn(Optional.empty());

            AppException ex = assertThrows(AppException.class,
                    () -> orderService.placeOrder(userId, request));
            assertEquals(ErrorCode.SKU_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("Should throw when SKU out of stock")
        void placeOrder_outOfStock() {
            CreateOrderRequest request = buildValidRequest();
            request.getItems().get(0).setQuantity(99); // more than stock (5)

            when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
            when(skuRepository.findBySkuId(skuId)).thenReturn(Optional.of(testSku));

            AppException ex = assertThrows(AppException.class,
                    () -> orderService.placeOrder(userId, request));
            assertEquals(ErrorCode.SKU_OUT_OF_STOCK, ex.getErrorCode());
        }
    }

    // ========== UPDATE ORDER STATUS ==========

    @Nested
    @DisplayName("updateOrderStatus")
    class UpdateOrderStatus {

        private Order buildOrder(OrderStatus status) {
            return Order.builder()
                    .orderId(UUID.randomUUID())
                    .user(testUser)
                    .status(status)
                    .items(new ArrayList<>())
                    .build();
        }

        @Test
        @DisplayName("PENDING → CONFIRMED should succeed")
        void pendingToConfirmed() {
            Order order = buildOrder(OrderStatus.PENDING);
            OrderResponse expected = OrderResponse.builder().status(OrderStatus.CONFIRMED).build();

            when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(expected);

            OrderResponse result = orderService.updateOrderStatus(order.getOrderId(), OrderStatus.CONFIRMED);

            assertEquals(OrderStatus.CONFIRMED, result.getStatus());
            assertEquals(OrderStatus.CONFIRMED, order.getStatus());
        }

        @Test
        @DisplayName("CONFIRMED → SHIPPING should succeed")
        void confirmedToShipping() {
            Order order = buildOrder(OrderStatus.CONFIRMED);
            OrderResponse expected = OrderResponse.builder().status(OrderStatus.SHIPPING).build();

            when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(expected);

            OrderResponse result = orderService.updateOrderStatus(order.getOrderId(), OrderStatus.SHIPPING);

            assertEquals(OrderStatus.SHIPPING, result.getStatus());
        }

        @Test
        @DisplayName("SHIPPING → COMPLETED should succeed")
        void shippingToCompleted() {
            Order order = buildOrder(OrderStatus.SHIPPING);
            OrderResponse expected = OrderResponse.builder().status(OrderStatus.COMPLETED).build();

            when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderMapper.toResponse(any(Order.class))).thenReturn(expected);

            OrderResponse result = orderService.updateOrderStatus(order.getOrderId(), OrderStatus.COMPLETED);

            assertEquals(OrderStatus.COMPLETED, result.getStatus());
        }

        @Test
        @DisplayName("PENDING → SHIPPING should fail (invalid transition)")
        void pendingToShipping_invalid() {
            Order order = buildOrder(OrderStatus.PENDING);

            when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(order));

            AppException ex = assertThrows(AppException.class,
                    () -> orderService.updateOrderStatus(order.getOrderId(), OrderStatus.SHIPPING));
            assertEquals(ErrorCode.ORDER_STATUS_TRANSITION_INVALID, ex.getErrorCode());
        }

        @Test
        @DisplayName("PENDING → COMPLETED should fail (invalid transition)")
        void pendingToCompleted_invalid() {
            Order order = buildOrder(OrderStatus.PENDING);

            when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(order));

            AppException ex = assertThrows(AppException.class,
                    () -> orderService.updateOrderStatus(order.getOrderId(), OrderStatus.COMPLETED));
            assertEquals(ErrorCode.ORDER_STATUS_TRANSITION_INVALID, ex.getErrorCode());
        }

        @Test
        @DisplayName("COMPLETED → SHIPPING should fail (terminal state)")
        void completedToShipping_invalid() {
            Order order = buildOrder(OrderStatus.COMPLETED);

            when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(order));

            AppException ex = assertThrows(AppException.class,
                    () -> orderService.updateOrderStatus(order.getOrderId(), OrderStatus.SHIPPING));
            assertEquals(ErrorCode.ORDER_STATUS_TRANSITION_INVALID, ex.getErrorCode());
        }

        @Test
        @DisplayName("Same status should return unchanged")
        void sameStatus_noOp() {
            Order order = buildOrder(OrderStatus.PENDING);
            OrderResponse expected = OrderResponse.builder().status(OrderStatus.PENDING).build();

            when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(order));
            when(orderMapper.toResponse(order)).thenReturn(expected);

            OrderResponse result = orderService.updateOrderStatus(order.getOrderId(), OrderStatus.PENDING);

            assertEquals(OrderStatus.PENDING, result.getStatus());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("Null status should throw")
        void nullStatus() {
            AppException ex = assertThrows(AppException.class,
                    () -> orderService.updateOrderStatus(UUID.randomUUID(), null));
            assertEquals(ErrorCode.ORDER_STATUS_INVALID, ex.getErrorCode());
        }

        @Test
        @DisplayName("Order not found should throw")
        void orderNotFound() {
            UUID orderId = UUID.randomUUID();
            when(orderRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

            AppException ex = assertThrows(AppException.class,
                    () -> orderService.updateOrderStatus(orderId, OrderStatus.CONFIRMED));
            assertEquals(ErrorCode.ORDER_NOT_FOUND, ex.getErrorCode());
        }

        @Test
        @DisplayName("Notification is sent on status change")
        void notificationSent() {
            Order order = buildOrder(OrderStatus.PENDING);

            when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderMapper.toResponse(any(Order.class)))
                    .thenReturn(OrderResponse.builder().status(OrderStatus.CONFIRMED).build());

            orderService.updateOrderStatus(order.getOrderId(), OrderStatus.CONFIRMED);

            verify(notificationService).createNotification(
                    eq(userId), anyString(), any(), any(), any());
        }
    }

    // ========== CANCEL ORDER ==========

    @Nested
    @DisplayName("cancelOrder")
    class CancelOrder {

        @Test
        @DisplayName("Cancel PENDING order should restore stock")
        void cancelPending_restoresStock() {
            FurnitureSku sku = FurnitureSku.builder()
                    .skuId(skuId)
                    .furniture(testFurniture)
                    .stock(3)
                    .build();

            OrderItem item = OrderItem.builder()
                    .sku(sku)
                    .furniture(testFurniture)
                    .quantity(2)
                    .build();

            Order order = Order.builder()
                    .orderId(UUID.randomUUID())
                    .user(testUser)
                    .status(OrderStatus.PENDING)
                    .items(new ArrayList<>(List.of(item)))
                    .build();

            when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(order));
            when(skuRepository.findBySkuId(skuId)).thenReturn(Optional.of(sku));
            when(skuRepository.findByFurnitureFurnitureIdAndStatus(any(), eq(FurnitureStatus.AVAILABLE)))
                    .thenReturn(List.of(sku));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderMapper.toResponse(any(Order.class)))
                    .thenReturn(OrderResponse.builder().status(OrderStatus.CANCELLED).build());

            OrderResponse result = orderService.cancelOrder(order.getOrderId());

            assertEquals(OrderStatus.CANCELLED, result.getStatus());

            // Stock restored: 3 + 2 = 5
            ArgumentCaptor<FurnitureSku> captor = ArgumentCaptor.forClass(FurnitureSku.class);
            verify(skuRepository).save(captor.capture());
            assertEquals(5, captor.getValue().getStock());
        }

        @Test
        @DisplayName("Cancel CONFIRMED order should succeed")
        void cancelConfirmed() {
            Order order = Order.builder()
                    .orderId(UUID.randomUUID())
                    .user(testUser)
                    .status(OrderStatus.CONFIRMED)
                    .items(new ArrayList<>())
                    .build();

            when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderMapper.toResponse(any(Order.class)))
                    .thenReturn(OrderResponse.builder().status(OrderStatus.CANCELLED).build());

            OrderResponse result = orderService.cancelOrder(order.getOrderId());
            assertEquals(OrderStatus.CANCELLED, result.getStatus());
        }

        @Test
        @DisplayName("Cancel already cancelled should throw")
        void cancelAlreadyCancelled() {
            Order order = Order.builder()
                    .orderId(UUID.randomUUID())
                    .user(testUser)
                    .status(OrderStatus.CANCELLED)
                    .build();

            when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(order));

            AppException ex = assertThrows(AppException.class,
                    () -> orderService.cancelOrder(order.getOrderId()));
            assertEquals(ErrorCode.ORDER_ALREADY_CANCELLED, ex.getErrorCode());
        }

        @Test
        @DisplayName("Cancel COMPLETED order should throw")
        void cancelCompleted() {
            Order order = Order.builder()
                    .orderId(UUID.randomUUID())
                    .user(testUser)
                    .status(OrderStatus.COMPLETED)
                    .build();

            when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(order));

            AppException ex = assertThrows(AppException.class,
                    () -> orderService.cancelOrder(order.getOrderId()));
            assertEquals(ErrorCode.ORDER_STATUS_TRANSITION_INVALID, ex.getErrorCode());
        }
    }

    // ========== COD FLOW (full lifecycle) ==========

    @Nested
    @DisplayName("COD Flow: PENDING → CONFIRMED → SHIPPING → COMPLETED")
    class CodFlow {

        @Test
        @DisplayName("Full COD lifecycle should succeed")
        void fullCodLifecycle() {
            Order order = Order.builder()
                    .orderId(UUID.randomUUID())
                    .user(testUser)
                    .status(OrderStatus.PENDING)
                    .items(new ArrayList<>())
                    .build();

            when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
            when(orderMapper.toResponse(any(Order.class))).thenAnswer(inv -> {
                Order o = inv.getArgument(0);
                return OrderResponse.builder().orderId(o.getOrderId()).status(o.getStatus()).build();
            });

            // Step 1: PENDING → CONFIRMED
            OrderResponse r1 = orderService.updateOrderStatus(order.getOrderId(), OrderStatus.CONFIRMED);
            assertEquals(OrderStatus.CONFIRMED, r1.getStatus());

            // Step 2: CONFIRMED → SHIPPING
            OrderResponse r2 = orderService.updateOrderStatus(order.getOrderId(), OrderStatus.SHIPPING);
            assertEquals(OrderStatus.SHIPPING, r2.getStatus());

            // Step 3: SHIPPING → COMPLETED
            OrderResponse r3 = orderService.updateOrderStatus(order.getOrderId(), OrderStatus.COMPLETED);
            assertEquals(OrderStatus.COMPLETED, r3.getStatus());

            // Notification sent 3 times
            verify(notificationService, times(3)).createNotification(
                    eq(userId), anyString(), any(), any(), any());
        }
    }

    // ========== ONLINE BANKING FLOW ==========

    @Nested
    @DisplayName("Online Banking Flow: PENDING → SHIPPING → COMPLETED")
    class OnlineBankingFlow {

        @Test
        @DisplayName("Online banking skips CONFIRMED — PENDING→SHIPPING is not in ALLOWED_TRANSITIONS")
        void onlineBankingSkipsConfirmed() {
            Order order = Order.builder()
                    .orderId(UUID.randomUUID())
                    .user(testUser)
                    .status(OrderStatus.PENDING)
                    .items(new ArrayList<>())
                    .build();

            when(orderRepository.findByOrderId(order.getOrderId())).thenReturn(Optional.of(order));

            // PENDING → SHIPPING should fail (must go through CONFIRMED first for COD)
            // For online banking, TransactionServiceImpl calls updateOrderStatus directly
            // which means PENDING→SHIPPING is NOT in ALLOWED_TRANSITIONS
            AppException ex = assertThrows(AppException.class,
                    () -> orderService.updateOrderStatus(order.getOrderId(), OrderStatus.SHIPPING));
            assertEquals(ErrorCode.ORDER_STATUS_TRANSITION_INVALID, ex.getErrorCode());
        }
    }
}
