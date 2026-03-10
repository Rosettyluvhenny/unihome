package com.exe.unihome.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.shipment.request.CreateShipmentRequest;
import com.exe.unihome.dto.shipment.response.ShipmentResponse;
import com.exe.unihome.dto.shipment.response.ShipperCodRevenueResponse;
import com.exe.unihome.dto.shipment.response.ShipperWorkloadResponse;
import com.exe.unihome.mapper.ShipmentMapper;
import com.exe.unihome.persistence.entity.identityAndAuth.RoleName;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import com.exe.unihome.persistence.entity.payment.Transaction;
import com.exe.unihome.persistence.entity.shipment.Shipment;
import com.exe.unihome.persistence.enums.ShipmentStatus;
import com.exe.unihome.persistence.enums.TransactionStatus;
import com.exe.unihome.persistence.repository.ShipmentRepository;
import com.exe.unihome.persistence.repository.TransactionRepository;
import com.exe.unihome.persistence.repository.UserRepository;
import com.exe.unihome.persistence.enums.OrderStatus;
import com.exe.unihome.service.OrderService;
import com.exe.unihome.service.ShipmentService;
import com.exe.unihome.service.TransactionService;
import com.exe.unihome.service.impl.TransactionExpirationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceImpl implements ShipmentService {

    private static final Map<ShipmentStatus, Set<ShipmentStatus>> ALLOWED_TRANSITIONS = Map.of(
            ShipmentStatus.PENDING,   EnumSet.of(ShipmentStatus.SHIPPING, ShipmentStatus.FAILED),
            ShipmentStatus.SHIPPING,  EnumSet.of(ShipmentStatus.COMPLETED, ShipmentStatus.FAILED),
            ShipmentStatus.COMPLETED, EnumSet.noneOf(ShipmentStatus.class),
            ShipmentStatus.FAILED,    EnumSet.noneOf(ShipmentStatus.class)
    );

    private final ShipmentRepository shipmentRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ShipmentMapper shipmentMapper;
    private final TransactionService transactionService;
    private final OrderService orderService;
    private final TransactionExpirationService transactionExpirationService;

    @Override
    @Transactional
    public ShipmentResponse createShipment(CreateShipmentRequest request) {
        Transaction transaction = transactionRepository.findById(request.getTransactionId())
                .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

        if (shipmentRepository.existsByTransaction_Id(request.getTransactionId())) {
            throw new AppException(ErrorCode.SHIPMENT_ALREADY_EXISTS);
        }

        User shipper = userRepository.findById(request.getShipperId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (shipper.getRole() != RoleName.SHIPPER) {
            throw new AppException(ErrorCode.SHIPMENT_INVALID_SHIPPER);
        }

        Shipment shipment = Shipment.builder()
                .transaction(transaction)
                .shipper(shipper)
                .status(ShipmentStatus.PENDING)
                .note(request.getNote())
                .build();

        shipment = shipmentRepository.save(shipment);
        log.info("Created shipment {} for transaction {} assigned to shipper {}",
                shipment.getId(), request.getTransactionId(), request.getShipperId());

        // COD: chuyển order sang SHIPPING ngay khi gắn shipper (không cần confirm trước)
        if (transaction.getPayment() != null && "CASH".equals(transaction.getPayment().getId())) {
            if (transaction.getOrder() != null) {
                orderService.updateOrderStatus(transaction.getOrder().getOrderId(), OrderStatus.SHIPPING);
                log.info("COD order {} moved to SHIPPING after shipper assigned", transaction.getOrder().getOrderId());
            }
            // Hủy timer hết hạn — COD không cần timeout, tiền thu khi shipper giao xong
            transactionExpirationService.cancelTransactionExpiration(transaction.getId());
            log.info("Cancelled expiration timer for COD transaction {}", transaction.getId());
        }

        return shipmentMapper.toResponse(shipment);
    }

    @Override
    @Transactional
    public ShipmentResponse updateShipmentStatus(String shipmentId, ShipmentStatus status, String note) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));

        Set<ShipmentStatus> allowed = ALLOWED_TRANSITIONS.getOrDefault(shipment.getStatus(), EnumSet.noneOf(ShipmentStatus.class));
        if (!allowed.contains(status)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        shipment.setStatus(status);
        if (note != null) {
            shipment.setNote(note);
        }

        shipment = shipmentRepository.save(shipment);
        log.info("Updated shipment {} status to {}", shipmentId, status);

        Transaction transaction = shipment.getTransaction();
        if (transaction != null) {
            if (status == ShipmentStatus.COMPLETED) {
                boolean isCod = transaction.getPayment() != null && "CASH".equals(transaction.getPayment().getId());
                if (isCod) {
                    // COD: transaction vẫn PENDING, confirm để thu tiền + cập nhật order → COMPLETED
                    transactionService.confirmTransaction(transaction.getId());
                } else {
                    // ONLINE: transaction đã SUCCESS từ trước, chỉ cần cập nhật order → COMPLETED
                    if (transaction.getOrder() != null) {
                        orderService.updateOrderStatus(transaction.getOrder().getOrderId(), OrderStatus.COMPLETED);
                    }
                }
                log.info("Confirmed transaction {} after shipment {} completed", transaction.getId(), shipmentId);
            } else if (status == ShipmentStatus.FAILED) {
                transactionService.cancelTransaction(transaction.getId(), note != null ? note : "Shipment failed");
                log.info("Cancelled transaction {} after shipment {} failed", transaction.getId(), shipmentId);
            }
        }

        return shipmentMapper.toResponse(shipment);
    }

    @Override
    public ShipmentResponse getShipment(String shipmentId) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));
        return shipmentMapper.toResponse(shipment);
    }

    @Override
    public ShipmentResponse getShipmentByOrderId(UUID orderId) {
        Shipment shipment = shipmentRepository.findByTransaction_Order_OrderId(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));
        return shipmentMapper.toResponse(shipment);
    }

    @Override
    public Page<ShipmentResponse> getShipmentsByShipper(String shipperId, ShipmentStatus status, Pageable pageable) {
        Page<Shipment> page;
        if (status != null) {
            page = shipmentRepository.findByShipper_IdAndStatus(shipperId, status, pageable);
        } else {
            page = shipmentRepository.findByShipper_Id(shipperId, pageable);
        }
        return page.map(shipmentMapper::toResponse);
    }

    @Override
    public boolean isShipperAssignedToOrder(UUID orderId, String shipperId) {
        return shipmentRepository.findByTransaction_Order_OrderId(orderId)
                .map(shipment -> shipment.getShipper().getId().equals(shipperId))
                .orElse(false);
    }

    @Override
    public boolean isShipperAssignedToShipment(String shipmentId, String shipperId) {
        return shipmentRepository.existsByIdAndShipper_Id(shipmentId, shipperId);
    }

    @Override
    public Page<ShipmentResponse> getAllShipments(String shipperId, ShipmentStatus status, LocalDate date, Pageable pageable) {
        LocalDateTime from = date != null ? date.atStartOfDay() : null;
        LocalDateTime to = date != null ? date.plusDays(1).atStartOfDay().minusNanos(1) : null;
        return shipmentRepository
                .findForAdmin(shipperId, status, from, to, pageable)
                .map(shipmentMapper::toResponse);
    }

    @Override
    public Page<ShipperWorkloadResponse> getShippers(Pageable pageable) {
        List<ShipmentStatus> activeStatuses = List.of(ShipmentStatus.PENDING, ShipmentStatus.SHIPPING);
        return userRepository.findByRole(RoleName.SHIPPER, pageable)
                .map(user -> ShipperWorkloadResponse.builder()
                        .id(user.getId())
                        .fullName(user.getFullName())
                        .phone(user.getPhone())
                        .image(user.getImage())
                        .totalShipments(shipmentRepository.countByShipper_Id(user.getId()))
                        .activeShipments(shipmentRepository.countByShipper_IdAndStatusIn(user.getId(), activeStatuses))
                        .build());
    }

    @Override
    public List<ShipperCodRevenueResponse> getCodRevenue(String shipperId, LocalDate date) {
        LocalDate target = date != null ? date : LocalDate.now();
        LocalDateTime from = target.atStartOfDay();
        LocalDateTime to = target.plusDays(1).atStartOfDay();
        return shipmentRepository.findCodRevenue(shipperId, from, to);
    }

}
