package com.exe.unihome.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.payment.request.CreatePaymentRequest;
import com.exe.unihome.dto.payment.request.UpdatePaymentRequest;
import com.exe.unihome.dto.payment.response.PaymentResponse;
import com.exe.unihome.mapper.PaymentMapper;
import com.exe.unihome.persistence.entity.payment.Payment;
import com.exe.unihome.persistence.repository.PaymentRepository;
import com.exe.unihome.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

  private final PaymentRepository paymentRepository;
  private final PaymentMapper paymentMapper;

  @Override
  @Transactional
  public PaymentResponse createPayment(CreatePaymentRequest request) {
    log.info("Creating payment: {}", request.getName());

    // Check if payment ID already exists
    if (paymentRepository.existsById(request.getId())) {
      throw new AppException(ErrorCode.INVALID_REQUEST);
    }

    Payment payment = paymentMapper.toEntity(request);
    Payment savedPayment = paymentRepository.save(payment);
    log.info("Payment created successfully with ID: {}", savedPayment.getId());

    return paymentMapper.toResponse(savedPayment);
  }

  @Override
  public PaymentResponse getPaymentById(String id) {
    log.info("Getting payment by ID: {}", id);

    Payment payment = paymentRepository.findById(id)
      .orElseThrow(() -> {
        log.error("Payment not found with ID: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });

    return paymentMapper.toResponse(payment);
  }

  @Override
  public List<PaymentResponse> getAllPayments() {
    log.info("Getting all payments");

    return paymentRepository.findAll().stream()
      .map(paymentMapper::toResponse)
      .collect(Collectors.toList());
  }

  @Override
  public List<PaymentResponse> getActivePayments() {
    log.info("Getting all active payments");

    return paymentRepository.findAll().stream()
      .filter(payment -> Boolean.TRUE.equals(payment.getIsActive()))
      .map(paymentMapper::toResponse)
      .collect(Collectors.toList());
  }

  @Override
  @Transactional
  public PaymentResponse updatePayment(String id, UpdatePaymentRequest request) {
    log.info("Updating payment ID: {}", id);

    Payment payment = paymentRepository.findById(id)
      .orElseThrow(() -> {
        log.error("Payment not found with ID: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });

    payment.setName(request.getName());
    if (request.getIsActive() != null) {
      payment.setIsActive(request.getIsActive());
    }
    if (request.getImg() != null) {
      payment.setImg(request.getImg());
    }

    Payment updatedPayment = paymentRepository.save(payment);
    log.info("Payment updated successfully with ID: {}", updatedPayment.getId());

    return paymentMapper.toResponse(updatedPayment);
  }

  @Override
  @Transactional
  public void deletePayment(String id) {
    log.info("Deleting payment ID: {}", id);

    Payment payment = paymentRepository.findById(id)
      .orElseThrow(() -> {
        log.error("Payment not found with ID: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });

    payment.setIsActive(Boolean.FALSE);
    paymentRepository.save(payment);
    log.info("Payment deleted successfully with ID: {}", id);
  }

  @Override
  public boolean existsById(String id) {
    return paymentRepository.existsById(id);
  }
}

