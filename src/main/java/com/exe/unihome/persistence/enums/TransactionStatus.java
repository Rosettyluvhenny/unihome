package com.exe.unihome.persistence.enums;

/**
 * Transaction Status Enum
 * Represents the lifecycle state of a payment transaction
 * <p>
 * PENDING  - Default status when transaction is created, awaiting payment
 * SUCCESS  - Payment has been successfully processed (Order status -> SHIPPING)
 * CANCEL   - User or system cancelled the transaction (Order status -> CANCELLED)
 * EXPIRED  - Transaction expired without payment (Order status -> CANCELLED)
 */
public enum TransactionStatus {
  PENDING,  // Default status when transaction is created
  SUCCESS,  // Payment successful
  CANCEL,   // User cancelled or admin cancelled
  EXPIRED   // Transaction expired due to timeout
}

