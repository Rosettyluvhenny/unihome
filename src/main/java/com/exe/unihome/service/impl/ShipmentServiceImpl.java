package com.exe.unihome.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.shipment.request.CreateShipmentRequest;
import com.exe.unihome.dto.shipment.response.ShipmentResponse;
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
import com.exe.unihome.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentServiceImpl implements ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final ShipmentMapper shipmentMapper;

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

        return shipmentMapper.toResponse(shipment);
    }

    @Override
    @Transactional
    public ShipmentResponse updateShipmentStatus(String shipmentId, ShipmentStatus status, String note) {
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIPMENT_NOT_FOUND));

        shipment.setStatus(status);
        if (note != null) {
            shipment.setNote(note);
        }

        shipment = shipmentRepository.save(shipment);
        log.info("Updated shipment {} status to {}", shipmentId, status);

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
}
