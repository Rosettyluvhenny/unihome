package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.shipment.Shipment;
import com.exe.unihome.persistence.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, String> {

    Optional<Shipment> findByTransaction_Id(String transactionId);

    Optional<Shipment> findByTransaction_Order_OrderId(UUID orderId);

    Page<Shipment> findByShipper_Id(String shipperId, Pageable pageable);

    Page<Shipment> findByShipper_IdAndStatus(String shipperId, ShipmentStatus status, Pageable pageable);

    boolean existsByTransaction_Id(String transactionId);
}
