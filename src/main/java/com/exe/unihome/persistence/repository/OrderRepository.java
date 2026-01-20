package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.order.Order;
import com.exe.unihome.persistence.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {

    @EntityGraph(attributePaths = {"user", "items", "items.furniture"})
    Optional<Order> findByOrderIdAndUserId(UUID orderId, String userId);

    @EntityGraph(attributePaths = {"user", "items", "items.furniture"})
    Optional<Order> findByOrderId(UUID orderId);

    @EntityGraph(attributePaths = {"user", "items", "items.furniture"})
    Page<Order> findByUserId(String userId, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items", "items.furniture"})
    Page<Order> findByUserIdAndStatus(String userId, OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items", "items.furniture"})
    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"user", "items", "items.furniture"})
    @Query("select o from Order o")
    Page<Order> findAllWithDetails(Pageable pageable);
}
