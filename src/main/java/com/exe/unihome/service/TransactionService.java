package com.exe.unihome.service;

import com.exe.unihome.persistence.entity.payment.Transaction;
import com.exe.unihome.persistence.enums.TransactionStatus;

import java.util.Optional;
import java.util.UUID;

/**
 * Transaction Service Interface
 * Defines operations for managing payment transactions
 */
public interface TransactionService {

  /**
   * Create a new transaction for an order
   * Initiates automatic expiration scheduling
   */
  Transaction createOrderTransaction(UUID orderId, String paymentMethodId, String paymentUrl);

  Transaction createBoostTransaction(String userBoostId, String paymentUrl);

  /**
   * Confirm transaction payment
   * Updates transaction status to SUCCESS and order status to SHIPPING
   */
  void confirmTransaction(String transactionId);

  /**
   * Cancel a transaction
   * Updates transaction status to CANCEL and order status to CANCELLED
   */
  void cancelTransaction(String transactionId, String reason);

  /**
   * Get transaction by ID
   */
  Optional<Transaction> getTransaction(String transactionId);

  /**
   * Get transaction by order ID
   */
  Optional<Transaction> getTransactionByOrderId(UUID orderId);

  /**
   * Update transaction status
   */
  void updateTransactionStatus(String transactionId, TransactionStatus status);

  /**
   * Check if transaction is pending and not expired
   */
  boolean isTransactionActive(String transactionId);
}

