package com.exe.unihome.controller;

import com.exe.unihome.dto.payment.request.CancelTransactionRequest;
import com.exe.unihome.dto.payment.request.CreateTransactionRequest;
import com.exe.unihome.dto.payment.request.CreateUserBoostTransactionRequest;
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
@RequestMapping("/transactions")
@RequiredArgsConstructor
@Slf4j
public class TransactionController {

  private final TransactionService transactionService;
  private final TransactionMapper transactionMapper;

  /**
   * Create a new transaction for an order
   * POST /api/transactions
   */
  @PostMapping("/order")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<TransactionResponse> createOrderTransaction(
    @RequestBody CreateTransactionRequest request) {
    log.info("Creating transaction for order: {}", request.getOrderId());

    UUID orderId = UUID.fromString(request.getOrderId());

    // Calculate expiration time (default 15 minutes from now)
    Transaction transaction = transactionService.createOrderTransaction(
      orderId,
      request.getPaymentMethodId()
    );

    TransactionResponse response = transactionMapper.toResponse(transaction);
    log.info("Transaction created successfully: {}", transaction.getId());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);


  }

  @PostMapping("/userboost")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<TransactionResponse> createUserBoostTransaction(
    @RequestBody CreateUserBoostTransactionRequest request) {
    String userBoostId = request.getUserBoostId();

    // Calculate expiration time (default 15 minutes from now)
    Transaction transaction = transactionService.createBoostTransaction(
      userBoostId
    );

    TransactionResponse response = transactionMapper.toResponse(transaction);
    log.info("Transaction created successfully: {}", transaction.getId());

    return ResponseEntity.status(HttpStatus.CREATED).body(response);

  }

  /**
   * Get transaction by ID
   * GET /api/transactions/{transactionId}
   */
  @GetMapping("/{transactionId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<TransactionResponse> getTransaction(
    @PathVariable String transactionId) {
    log.info("Fetching transaction: {}", transactionId);

    Optional<Transaction> transaction = transactionService.getTransaction(transactionId);

    if (transaction.isEmpty()) {
      log.warn("Transaction not found: {}", transactionId);
      return ResponseEntity.notFound().build();
    }

    TransactionResponse response = transactionMapper.toResponse(transaction.get());
    return ResponseEntity.ok(response);


  }

  /**
   * Get transaction by order ID
   * GET /api/transactions/order/{orderId}
   */
  @GetMapping("/order/{orderId}")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<TransactionResponse> getTransactionByOrderId(
    @PathVariable String orderId) {
    log.info("Fetching transaction for order: {}", orderId);

    UUID id = UUID.fromString(orderId);
    Optional<Transaction> transaction = transactionService.getTransactionByOrderId(id);

    if (transaction.isEmpty()) {
      log.warn("Transaction not found for order: {}", orderId);
      return ResponseEntity.notFound().build();
    }

    TransactionResponse response = transactionMapper.toResponse(transaction.get());
    return ResponseEntity.ok(response);


  }

  /**
   * Confirm transaction payment
   * PUT /api/transactions/{transactionId}/confirm
   */
  @PutMapping("/{transactionId}/confirm")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<TransactionResponse> confirmTransaction(
    @PathVariable String transactionId) {
    log.info("Confirming transaction: {}", transactionId);

    transactionService.confirmTransaction(transactionId);

    Optional<Transaction> transaction = transactionService.getTransaction(transactionId);
    if (transaction.isEmpty()) {
      return ResponseEntity.notFound().build();
    }

    TransactionResponse response = transactionMapper.toResponse(transaction.get());
    log.info("Transaction confirmed successfully: {}", transactionId);

    return ResponseEntity.ok(response);


  }

  /**
   * Cancel a transaction
   * PUT /api/transactions/{transactionId}/cancel
   */
  @PutMapping("/{transactionId}/cancel")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<TransactionResponse> cancelTransaction(
    @PathVariable String transactionId,
    @RequestBody(required = false) CancelTransactionRequest request) {
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


  }

  /**
   * Check if transaction is active
   * GET /api/transactions/{transactionId}/active
   */
  @GetMapping("/{transactionId}/active")
  @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
  public ResponseEntity<Boolean> isTransactionActive(
    @PathVariable String transactionId) {
    log.info("Checking if transaction is active: {}", transactionId);

    boolean isActive = transactionService.isTransactionActive(transactionId);
    return ResponseEntity.ok(isActive);

  }
}

