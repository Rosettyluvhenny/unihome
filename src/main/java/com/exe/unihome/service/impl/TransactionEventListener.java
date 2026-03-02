package com.exe.unihome.service.impl;

import com.exe.unihome.service.model.TransactionCancelledEvent;
import com.exe.unihome.service.model.TransactionCreatedEvent;
import com.exe.unihome.service.model.TransactionSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Transaction Event Listener
 * Handles transaction-related events asynchronously
 * - TransactionCreatedEvent: schedules automatic expiration
 * - TransactionSuccessEvent: cancels scheduled expiration
 * - TransactionCancelledEvent: cleans up scheduled expiration
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventListener {

  private final TransactionExpirationService transactionExpirationService;

  /**
   * Handle transaction created event
   * Schedules automatic expiration for the transaction
   */
  @EventListener
  @Async
  public void handleTransactionCreated(TransactionCreatedEvent event) {
    log.info("Processing transaction created event for transaction: {}", event.getTransactionId());
    try {
      transactionExpirationService.scheduleTransactionExpiration(event.getTransactionId());
      log.info("Successfully scheduled expiration for transaction: {}", event.getTransactionId());
    } catch (Exception e) {
      log.error("Failed to process transaction created event for transaction {}: {}",
        event.getTransactionId(), e.getMessage(), e);
    }
  }

  /**
   * Handle transaction success event
   * Cancels scheduled expiration when payment is confirmed
   */
  @EventListener
  @Async
  public void handleTransactionSuccess(TransactionSuccessEvent event) {
    log.info("Processing transaction success event for transaction: {}", event.getTransactionId());
    try {
      transactionExpirationService.cancelTransactionExpiration(event.getTransactionId());
      log.info("Successfully cancelled expiration for transaction: {}", event.getTransactionId());
    } catch (Exception e) {
      log.error("Failed to process transaction success event for transaction {}: {}",
        event.getTransactionId(), e.getMessage(), e);
    }
  }

  /**
   * Handle transaction cancelled event
   * Cleans up scheduled expiration (only if manual cancellation)
   */
  @EventListener
  @Async
  public void handleTransactionCancelled(TransactionCancelledEvent event) {
    log.info("Processing transaction cancelled event for transaction: {} (automatic: {})",
      event.getTransactionId(), event.isAutomatic());
    try {
      // Only cancel scheduled expiration if this is NOT the automatic expiration itself
      if (!event.isAutomatic()) {
        transactionExpirationService.cancelTransactionExpiration(event.getTransactionId());
        log.info("Successfully cancelled scheduled expiration for transaction: {}",
          event.getTransactionId());
      }
    } catch (Exception e) {
      log.error("Failed to process transaction cancelled event for transaction {}: {}",
        event.getTransactionId(), e.getMessage(), e);
    }
  }
}

