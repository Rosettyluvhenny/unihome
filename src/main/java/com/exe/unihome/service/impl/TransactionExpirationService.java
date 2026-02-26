package com.exe.unihome.service.impl;

import com.exe.unihome.persistence.entity.payment.Transaction;
import com.exe.unihome.persistence.enums.OrderStatus;
import com.exe.unihome.persistence.enums.TransactionStatus;
import com.exe.unihome.persistence.repository.TransactionRepository;
import com.exe.unihome.service.OrderService;
import com.exe.unihome.service.model.TransactionExpirationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * Transaction Expiration Service
 * Manages automatic transaction expiration using Redis delayed queues
 * Scheduled task processes expired transactions and updates order status
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionExpirationService {

  private static final String TRANSACTION_EXPIRATION_QUEUE = "transaction:expiration:queue";
  private static final String TRANSACTION_CACHE_PREFIX = "transaction:cache:";

  @Value("${payment.minutes}")
  private long EXPIRATION_MINUTES;

  private final RedissonClient redissonClient;
  private final RedisTemplate<String, Object> redisTemplate;
  private final TransactionRepository transactionRepository;
  private final OrderService orderService;
  private final Clock clock;

  /**
   * Schedule transaction for automatic expiration
   * Adds event to Redis delayed queue which will trigger after EXPIRATION_MINUTES
   */
  @Async
  public void scheduleTransactionExpiration(String transactionId) {
    try {
      // Get or create Redisson queue
      RQueue<TransactionExpirationEvent> queue =
        redissonClient.getQueue(TRANSACTION_EXPIRATION_QUEUE);

      // Get delayed queue wrapper
      RDelayedQueue<TransactionExpirationEvent> delayedQueue =
        redissonClient.getDelayedQueue(queue);

      // Calculate expiration time
      LocalDateTime expirationTime = LocalDateTime.now(clock)
        .plusMinutes(EXPIRATION_MINUTES);

      // Create expiration event
      TransactionExpirationEvent event = TransactionExpirationEvent.builder()
        .transactionId(transactionId)
        .expirationTime(expirationTime)
        .reason("Transaction expiration after " + EXPIRATION_MINUTES + " minutes")
        .build();

      // Add event to delayed queue (will be auto-triggered after delay)
      delayedQueue.offer(event, EXPIRATION_MINUTES, TimeUnit.MINUTES);

      // Cache transaction for quick access
      cacheTransactionForExpiration(transactionId);

      log.info("Scheduled transaction {} for expiration at {}", transactionId, expirationTime);

    } catch (Exception e) {
      log.error("Failed to schedule transaction expiration for transaction {}: {}",
        transactionId, e.getMessage(), e);
    }
  }

  /**
   * Cache transaction in Redis for quick access during expiration processing
   */
  private void cacheTransactionForExpiration(String transactionId) {
    try {
      Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
      if (transaction != null) {
        redisTemplate.opsForValue().set(
          TRANSACTION_CACHE_PREFIX + transactionId,
          transaction,
          EXPIRATION_MINUTES,
          TimeUnit.MINUTES
        );
      }
    } catch (Exception e) {
      log.error("Failed to cache transaction for transaction {}: {}",
        transactionId, e.getMessage(), e);
    }
  }

  /**
   * Cancel scheduled transaction expiration
   * Removes the transaction from expiration processing
   */
  public void cancelTransactionExpiration(String transactionId) {
    try {
      redisTemplate.delete(TRANSACTION_CACHE_PREFIX + transactionId);
      log.info("Cancelled scheduled expiration for transaction {}", transactionId);
    } catch (Exception e) {
      log.error("Failed to cancel transaction expiration for transaction {}: {}",
        transactionId, e.getMessage(), e);
    }
  }

  /**
   * Scheduled task to process expired transactions
   * Runs every 5 seconds to check for and process expired transactions
   */
  @Scheduled(fixedDelay = 5000)  // Check every 5 seconds
  public void processExpiredTransactions() {
    try {
      // Get the Redisson queue
      RQueue<TransactionExpirationEvent> queue =
        redissonClient.getQueue(TRANSACTION_EXPIRATION_QUEUE);

      // Poll events (returns null if queue is empty)
      TransactionExpirationEvent event;
      while ((event = queue.poll()) != null) {
        processTransactionExpiration(event);
      }

    } catch (Exception e) {
      log.error("Error processing expired transactions: {}", e.getMessage(), e);
    }
  }

  /**
   * Process individual expired transaction
   * Updates transaction status to EXPIRED and order status to CANCELLED
   */
  @Transactional
  protected void processTransactionExpiration(TransactionExpirationEvent event) {
    try {
      String transactionId = event.getTransactionId();

      // Get cached transaction (or fallback to DB)
      Transaction transaction = getCachedTransaction(transactionId);

      if (transaction == null) {
        log.warn("Transaction {} not found for expiration processing", transactionId);
        return;
      }

      // Only expire if transaction is still PENDING
      if (transaction.getStatus() == TransactionStatus.PENDING) {
        expireTransaction(transaction);
      }

      // Clean up cache
      redisTemplate.delete(TRANSACTION_CACHE_PREFIX + transactionId);

    } catch (Exception e) {
      log.error("Error processing transaction expiration for transaction {}: {}",
        event.getTransactionId(), e.getMessage(), e);
    }
  }

  /**
   * Expire a transaction and update order status to CANCELLED
   */
  @Transactional
  protected void expireTransaction(Transaction transaction) {
    try {
      // Update transaction status to EXPIRED
      transaction.setStatus(TransactionStatus.EXPIRED);
      transaction.setExpiredAt(LocalDateTime.now(clock));
      transactionRepository.save(transaction);

      // Update order status to CANCELLED
      orderService.updateOrderStatus(transaction.getOrder().getOrderId(), OrderStatus.CANCELLED);

      log.info("Transaction {} expired and order {} cancelled",
        transaction.getId(), transaction.getOrder().getOrderId());

    } catch (Exception e) {
      log.error("Failed to expire transaction {}: {}",
        transaction.getId(), e.getMessage(), e);
    }
  }

  /**
   * Get cached transaction from Redis
   */
  @SuppressWarnings("unchecked")
  private Transaction getCachedTransaction(String transactionId) {
    try {
      Object cached = redisTemplate.opsForValue()
        .get(TRANSACTION_CACHE_PREFIX + transactionId);
      if (cached instanceof Transaction) {
        return (Transaction) cached;
      }
      // Fallback to database if not cached
      return transactionRepository.findById(transactionId).orElse(null);
    } catch (Exception e) {
      log.warn("Failed to retrieve cached transaction {}: {}", transactionId, e.getMessage());
      return transactionRepository.findById(transactionId).orElse(null);
    }
  }
}


