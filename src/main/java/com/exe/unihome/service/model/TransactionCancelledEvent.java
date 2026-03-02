package com.exe.unihome.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TransactionCancelledEvent
 * Published when a transaction is cancelled (manually or automatically)
 * Used to clean up scheduled expiration and update order status
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCancelledEvent {
  private String transactionId;
  private UUID orderId;
  private String userBoostId;
  private LocalDateTime timestamp;
  private String reason;
  private boolean automatic;  // true if cancelled due to expiration
}

