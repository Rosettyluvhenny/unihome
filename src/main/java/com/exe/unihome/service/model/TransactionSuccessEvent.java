package com.exe.unihome.service.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * TransactionSuccessEvent
 * Published when a transaction payment is confirmed
 * Used to update order status to SHIPPING and clean up expiration schedules
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSuccessEvent {
  private String transactionId;
}

