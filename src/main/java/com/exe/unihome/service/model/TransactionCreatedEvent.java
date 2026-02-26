package com.exe.unihome.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * TransactionCreatedEvent
 * Published when a new transaction is created
 * Used to trigger automatic expiration scheduling
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionCreatedEvent {
  private String transactionId;
  private UUID orderId;
  private String userBoostId;
  private LocalDateTime createdAt;
  private LocalDateTime expirationTime;
}

