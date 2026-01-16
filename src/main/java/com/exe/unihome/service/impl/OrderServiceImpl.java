package com.exe.unihome.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.order.response.OrderResponse;
import com.exe.unihome.mapper.OrderMapper;
import com.exe.unihome.persistence.entity.Furniture;
import com.exe.unihome.persistence.entity.cart.Cart;
import com.exe.unihome.persistence.entity.cart.CartItem;
import com.exe.unihome.persistence.entity.order.Order;
import com.exe.unihome.persistence.entity.order.OrderItem;
import com.exe.unihome.persistence.enums.OrderStatus;
import com.exe.unihome.persistence.repository.CartRepository;
import com.exe.unihome.persistence.repository.FurnitureRepository;
import com.exe.unihome.persistence.repository.OrderRepository;
import com.exe.unihome.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.EnumSet;
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
    private final CartRepository cartRepository;
    private final FurnitureRepository furnitureRepository;
    private final OrderMapper orderMapper;

    @Override
    @Transactional
    public OrderResponse placeOrder(String userId) {
        Cart cart = cartRepository.findForUpdateByUserId(userId)
            .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        if (cart.getItems() == null || cart.getItems().isEmpty()) {
            throw new AppException(ErrorCode.CART_EMPTY);
        }

        Order order = Order.builder()
            .user(cart.getUser())
            .status(OrderStatus.PENDING)
            .build();

        BigDecimal total = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {
            Furniture furniture = furnitureRepository.findById(cartItem.getFurniture().getFurnitureId())
                .orElseThrow(() -> new AppException(ErrorCode.FURNITURE_NOT_FOUND));

            if (furniture.getStock() < cartItem.getQuantity()) {
                throw new AppException(ErrorCode.FURNITURE_OUT_OF_STOCK);
            }

            furniture.setStock(furniture.getStock() - cartItem.getQuantity());
            furnitureRepository.save(furniture);

            BigDecimal unitPrice = resolveUnitPrice(furniture);
            OrderItem orderItem = OrderItem.builder()
                .order(order)
                .furniture(furniture)
                .quantity(cartItem.getQuantity())
                .price(unitPrice)
                .build();
            order.getItems().add(orderItem);

            total = total.add(unitPrice.multiply(BigDecimal.valueOf(cartItem.getQuantity())));
        }

        order.setTotalPrice(total);
        Order savedOrder = orderRepository.save(order);

        clearCart(cart);

        return orderMapper.toResponse(savedOrder);
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
                Furniture furniture = furnitureRepository.findById(orderItem.getFurniture().getFurnitureId())
                    .orElseThrow(() -> new AppException(ErrorCode.FURNITURE_NOT_FOUND));
                furniture.setStock(furniture.getStock() + orderItem.getQuantity());
                furnitureRepository.save(furniture);
            });
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    private void clearCart(Cart cart) {
        if (cart.getItems() != null) {
            cart.getItems().clear();
        }
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setTotalQuantity(0);
        cartRepository.save(cart);
    }

    private boolean isTransitionAllowed(OrderStatus current, OrderStatus target) {
        return ALLOWED_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(OrderStatus.class)).contains(target);
    }

    private BigDecimal resolveUnitPrice(Furniture furniture) {
        return furniture.getFinalPrice() != null ? furniture.getFinalPrice() : furniture.getPrice();
    }
}
