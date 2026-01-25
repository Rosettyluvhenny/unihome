package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.order.OrderItem;
import com.exe.unihome.persistence.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.UUID;

public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    boolean existsByOrder_User_IdAndFurniture_FurnitureIdAndOrder_StatusIn(
        String userId,
        UUID furnitureId,
        Collection<OrderStatus> statuses
    );
}
