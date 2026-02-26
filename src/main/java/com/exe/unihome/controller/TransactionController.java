package com.exe.unihome.controller;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.payment.request.CancelTransactionRequest;
import com.exe.unihome.dto.payment.request.CreateTransactionRequest;
import com.exe.unihome.dto.payment.response.TransactionResponse;
import com.exe.unihome.mapper.TransactionMapper;
import com.exe.unihome.persistence.entity.payment.Transaction;
import com.exe.unihome.service.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

/**
 * Transaction Controller
 * Handles payment transaction creation, confirmation, and cancellation
 * Endpoints for managing transaction lifecycle
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

  private final TransactionService transactionService;
  private final TransactionMapper transactionMapper;

  /**
   * Create a new transaction for an order
   * POST /api/transactions
   */
  @PostMapping
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  public ResponseEntity<TransactionResponse> createTransaction(
    @RequestBody CreateTransactionRequest request) {
    if (request.getOrderId() == null && request.getUserBoostId() == null)
      throw new AppException(ErrorCode.INVALID_TRANSACTION);
    try {
      log.info("Creating transaction for order: {}", request.getOrderId());

      UUID orderId = UUID.fromString(request.getOrderId());

      // Calculate expiration time (default 15 minutes from now)
      Transaction transaction = transactionService.createTransaction(
        orderId,
        request.getPaymentMethodId(),
        request.getPaymentUrl()
      );

      TransactionResponse response = transactionMapper.toResponse(transaction);
      log.info("Transaction created successfully: {}", transaction.getId());

      return ResponseEntity.status(HttpStatus.CREATED).body(response);

    } catch (IllegalArgumentException e) {
      log.error("Invalid request: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error creating transaction: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Get transaction by ID
   * GET /api/transactions/{transactionId}
   */
  @GetMapping("/{transactionId}")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  public ResponseEntity<TransactionResponse> getTransaction(
    @PathVariable String transactionId) {
    try {
      log.info("Fetching transaction: {}", transactionId);

      Optional<Transaction> transaction = transactionService.getTransaction(transactionId);

      if (transaction.isEmpty()) {
        log.warn("Transaction not found: {}", transactionId);
        return ResponseEntity.notFound().build();
      }

      TransactionResponse response = transactionMapper.toResponse(transaction.get());
      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error fetching transaction: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Get transaction by order ID
   * GET /api/transactions/order/{orderId}
   */
  @GetMapping("/order/{orderId}")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  public ResponseEntity<TransactionResponse> getTransactionByOrderId(
    @PathVariable String orderId) {
    try {
      log.info("Fetching transaction for order: {}", orderId);

      UUID id = UUID.fromString(orderId);
      Optional<Transaction> transaction = transactionService.getTransactionByOrderId(id);

      if (transaction.isEmpty()) {
        log.warn("Transaction not found for order: {}", orderId);
        return ResponseEntity.notFound().build();
      }

      TransactionResponse response = transactionMapper.toResponse(transaction.get());
      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      log.error("Invalid order ID: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error fetching transaction by order ID: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Confirm transaction payment
   * PUT /api/transactions/{transactionId}/confirm
   */
  @PutMapping("/{transactionId}/confirm")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  public ResponseEntity<TransactionResponse> confirmTransaction(
    @PathVariable String transactionId) {
    try {
      log.info("Confirming transaction: {}", transactionId);

      transactionService.confirmTransaction(transactionId);

      Optional<Transaction> transaction = transactionService.getTransaction(transactionId);
      if (transaction.isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      TransactionResponse response = transactionMapper.toResponse(transaction.get());
      log.info("Transaction confirmed successfully: {}", transactionId);

      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      log.error("Invalid transaction ID: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (IllegalStateException e) {
      log.error("Invalid transaction state: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error confirming transaction: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Cancel a transaction
   * PUT /api/transactions/{transactionId}/cancel
   */
  @PutMapping("/{transactionId}/cancel")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  public ResponseEntity<TransactionResponse> cancelTransaction(
    @PathVariable String transactionId,
    @RequestBody(required = false) CancelTransactionRequest request) {
    try {
      log.info("Cancelling transaction: {}", transactionId);

      String reason = request != null && request.getReason() != null ?
        request.getReason() : "User cancelled transaction";

      transactionService.cancelTransaction(transactionId, reason);

      Optional<Transaction> transaction = transactionService.getTransaction(transactionId);
      if (transaction.isEmpty()) {
        return ResponseEntity.notFound().build();
      }

      TransactionResponse response = transactionMapper.toResponse(transaction.get());
      log.info("Transaction cancelled successfully: {}", transactionId);

      return ResponseEntity.ok(response);

    } catch (IllegalArgumentException e) {
      log.error("Invalid transaction ID: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (IllegalStateException e) {
      log.error("Invalid transaction state: {}", e.getMessage());
      return ResponseEntity.badRequest().build();
    } catch (Exception e) {
      log.error("Error cancelling transaction: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }

  /**
   * Check if transaction is active
   * GET /api/transactions/{transactionId}/active
   */
  @GetMapping("/{transactionId}/active")
  @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
  public ResponseEntity<Boolean> isTransactionActive(
    @PathVariable String transactionId) {
    try {
      log.info("Checking if transaction is active: {}", transactionId);

      boolean isActive = transactionService.isTransactionActive(transactionId);
      return ResponseEntity.ok(isActive);

    } catch (Exception e) {
      log.error("Error checking transaction activity: {}", e.getMessage(), e);
      return ResponseEntity.internalServerError().build();
    }
  }
}

