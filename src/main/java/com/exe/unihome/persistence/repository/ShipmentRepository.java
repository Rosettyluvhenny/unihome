package com.exe.unihome.persistence.repository;

import com.exe.unihome.dto.shipment.response.ShipperCodRevenueResponse;
import com.exe.unihome.persistence.entity.shipment.Shipment;
import com.exe.unihome.persistence.enums.ShipmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, String> {

    Optional<Shipment> findByTransaction_Id(String transactionId);

    Optional<Shipment> findByTransaction_Order_OrderId(UUID orderId);

    Page<Shipment> findByShipper_Id(String shipperId, Pageable pageable);

    Page<Shipment> findByShipper_IdAndStatus(String shipperId, ShipmentStatus status, Pageable pageable);

    boolean existsByTransaction_Id(String transactionId);

    boolean existsByIdAndShipper_Id(String shipmentId, String shipperId);

    @Query("SELECT s FROM Shipment s WHERE " +
           "(:shipperId IS NULL OR s.shipper.id = :shipperId) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:from IS NULL OR s.createdAt >= :from) AND " +
           "(:to IS NULL OR s.createdAt <= :to)")
    Page<Shipment> findForAdmin(@Param("shipperId") String shipperId,
                                @Param("status") ShipmentStatus status,
                                @Param("from") LocalDateTime from,
                                @Param("to") LocalDateTime to,
                                Pageable pageable);

    long countByShipper_IdAndStatusIn(String shipperId, List<ShipmentStatus> statuses);

    long countByShipper_Id(String shipperId);

    @Query("SELECT new com.exe.unihome.dto.shipment.response.ShipperCodRevenueResponse(" +
           "s.shipper.id, s.shipper.fullName, s.shipper.phone, COUNT(s), COALESCE(SUM(s.transaction.totalPrice), 0)) " +
           "FROM Shipment s WHERE " +
           "s.status = com.exe.unihome.persistence.enums.ShipmentStatus.COMPLETED AND " +
           "s.transaction.payment.id = 'CASH' AND " +
           "(:shipperId IS NULL OR s.shipper.id = :shipperId) AND " +
           "s.updatedAt >= :from AND s.updatedAt < :to " +
           "GROUP BY s.shipper.id, s.shipper.fullName, s.shipper.phone")
    List<ShipperCodRevenueResponse> findCodRevenue(@Param("shipperId") String shipperId,
                                                    @Param("from") LocalDateTime from,
                                                    @Param("to") LocalDateTime to);
}
