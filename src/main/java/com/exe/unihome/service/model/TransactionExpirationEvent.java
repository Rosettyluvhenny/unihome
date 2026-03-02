package com.exe.unihome.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * TransactionExpirationEvent
 * Represents an event for transaction expiration
 * Used by the scheduled task to process expired transactions
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionExpirationEvent {
  private String transactionId;
  private LocalDateTime expirationTime;
  private String reason;
}

