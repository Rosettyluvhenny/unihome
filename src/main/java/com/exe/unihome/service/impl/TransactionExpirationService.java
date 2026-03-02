package com.exe.unihome.service.impl;

import com.exe.unihome.persistence.entity.payment.Transaction;
import com.exe.unihome.persistence.entity.subscription.UserBoost;
import com.exe.unihome.persistence.entity.subscription.UserBoostStatus;
import com.exe.unihome.persistence.enums.OrderStatus;
import com.exe.unihome.persistence.enums.TransactionStatus;
import com.exe.unihome.persistence.repository.TransactionRepository;
import com.exe.unihome.persistence.repository.UserBoostRepository;
import com.exe.unihome.postAndSubscription.UserBoostService;
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
 * Scheduled task processes expired transactions and updates order/userBoost status
 * <p>
 * Handles two types of transactions:
 * 1. Order Transactions: Updates order status
 * 2. UserBoost Transactions: Updates userBoost status (ACTIVE on success, CANCELLED on expiration)
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
  private final UserBoostService userBoostService;
  private final UserBoostRepository userBoostRepository;
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
   * Cache transaction ID in Redis for quick access during expiration processing
   * Only stores the ID string to avoid LazyInitializationException with lazy-loaded entities
   */
  private void cacheTransactionForExpiration(String transactionId) {
    try {
      // Only cache the transaction ID, not the entire entity
      // This avoids LazyInitializationException when serializing lazy-loaded Payment
      redisTemplate.opsForValue().set(
        TRANSACTION_CACHE_PREFIX + transactionId,
        transactionId,
        EXPIRATION_MINUTES,
        TimeUnit.MINUTES
      );
      log.debug("Cached transaction ID {} for expiration", transactionId);
    } catch (Exception e) {
      log.error("Failed to cache transaction ID {} for transaction: {}",
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
   * Checks if transaction is for Order or UserBoost and updates corresponding entity
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

      // Only process if transaction is still PENDING
      if (transaction.getStatus() == TransactionStatus.PENDING) {
        // Check if this is an order transaction or userBoost transaction
        if (isOrderTransaction(transaction)) {
          expireOrderTransaction(transaction);
        } else if (isUserBoostTransaction(transaction)) {
          expireUserBoostTransaction(transaction);
        } else {
          log.warn("Transaction {} has neither orderId nor userBoostId", transactionId);
        }
      }

      // Clean up cache
      redisTemplate.delete(TRANSACTION_CACHE_PREFIX + transactionId);

    } catch (Exception e) {
      log.error("Error processing transaction expiration for transaction {}: {}",
        event.getTransactionId(), e.getMessage(), e);
    }
  }

  /**
   * Check if transaction is for an order
   */
  private boolean isOrderTransaction(Transaction transaction) {
    return transaction.getOrder() != null;
  }

  /**
   * Check if transaction is for a userBoost
   */
  private boolean isUserBoostTransaction(Transaction transaction) {
    return transaction.getUserBoost() != null;
  }

  /**
   * Expire an order transaction and update order status to CANCELLED
   */
  @Transactional
  protected void expireOrderTransaction(Transaction transaction) {
    try {
      // Validate order exists
      if (transaction.getOrder() == null || transaction.getOrder().getOrderId() == null) {
        log.warn("Transaction {} has invalid order reference", transaction.getId());
        transaction.setStatus(TransactionStatus.EXPIRED);
        transaction.setExpiredAt(LocalDateTime.now(clock));
        transactionRepository.save(transaction);
        return;
      }

      // Update transaction status to EXPIRED
      transaction.setStatus(TransactionStatus.EXPIRED);
      transaction.setExpiredAt(LocalDateTime.now(clock));
      transactionRepository.save(transaction);

      // Update order status to CANCELLED
      orderService.updateOrderStatus(transaction.getOrder().getOrderId(), OrderStatus.CANCELLED);

      log.info("Transaction {} expired and order {} cancelled",
        transaction.getId(), transaction.getOrder().getOrderId());

    } catch (Exception e) {
      log.error("Failed to expire order transaction {}: {}",
        transaction.getId(), e.getMessage(), e);
    }
  }

  /**
   * Expire a userBoost transaction and update userBoost status to CANCELLED
   */
  @Transactional
  protected void expireUserBoostTransaction(Transaction transaction) {
    try {
      // Validate userBoost exists
      if (transaction.getUserBoost() == null || transaction.getUserBoost().getId() == null) {
        log.warn("Transaction {} has invalid userBoost reference", transaction.getId());
        transaction.setStatus(TransactionStatus.EXPIRED);
        transaction.setExpiredAt(LocalDateTime.now(clock));
        transactionRepository.save(transaction);
        return;
      }

      String userBoostId = transaction.getUserBoost().getId();

      // Update transaction status to EXPIRED
      transaction.setStatus(TransactionStatus.EXPIRED);
      transaction.setExpiredAt(LocalDateTime.now(clock));
      transactionRepository.save(transaction);

      // Update userBoost status to CANCELLED
      UserBoost userBoost = userBoostRepository.findById(userBoostId).orElse(null);
      if (userBoost != null) {
        userBoost.setStatus(UserBoostStatus.CANCELLED);
        userBoostRepository.save(userBoost);
        log.info("Transaction {} expired and userBoost {} cancelled",
          transaction.getId(), userBoostId);
      } else {
        log.warn("UserBoost {} not found for transaction {}", userBoostId, transaction.getId());
      }

    } catch (Exception e) {
      log.error("Failed to expire userBoost transaction {}: {}",
        transaction.getId(), e.getMessage(), e);
    }
  }

  /**
   * Handle successful transaction completion
   * Updates transaction status to SUCCESS and sets userBoost to ACTIVE if applicable
   */
  @Transactional
  public void completeTransaction(String transactionId) {
    try {
      Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
      if (transaction == null) {
        log.warn("Transaction {} not found for completion", transactionId);
        return;
      }

      // Update transaction status to SUCCESS
      transaction.setStatus(TransactionStatus.SUCCESS);
      transaction.setPaidAt(LocalDateTime.now(clock));
      transactionRepository.save(transaction);

      // If this is a userBoost transaction, update its status to ACTIVE
      if (isUserBoostTransaction(transaction)) {
        String userBoostId = transaction.getUserBoost().getId();
        UserBoost userBoost = userBoostRepository.findById(userBoostId).orElse(null);
        if (userBoost != null) {
          userBoost.setStatus(UserBoostStatus.ACTIVE);
          userBoostRepository.save(userBoost);
          log.info("Transaction {} completed and userBoost {} activated",
            transactionId, userBoostId);
        } else {
          log.warn("UserBoost {} not found for transaction {}", userBoostId, transactionId);
        }
      } else if (isOrderTransaction(transaction)) {
        log.info("Transaction {} completed for order {}",
          transactionId, transaction.getOrder().getOrderId());
      }

      // Cancel expiration scheduling
      cancelTransactionExpiration(transactionId);

    } catch (Exception e) {
      log.error("Failed to complete transaction {}: {}",
        transactionId, e.getMessage(), e);
    }
  }

  /**
   * Cancel a transaction and update related entity status
   */
  @Transactional
  public void cancelTransaction(String transactionId) {
    try {
      Transaction transaction = transactionRepository.findById(transactionId).orElse(null);
      if (transaction == null) {
        log.warn("Transaction {} not found for cancellation", transactionId);
        return;
      }

      // Update transaction status to CANCEL
      transaction.setStatus(TransactionStatus.CANCEL);
      transactionRepository.save(transaction);

      // Handle userBoost cancellation
      if (isUserBoostTransaction(transaction)) {
        String userBoostId = transaction.getUserBoost().getId();
        UserBoost userBoost = userBoostRepository.findById(userBoostId).orElse(null);
        if (userBoost != null) {
          userBoost.setStatus(UserBoostStatus.CANCELLED);
          userBoostRepository.save(userBoost);
          log.info("Transaction {} cancelled and userBoost {} cancelled",
            transactionId, userBoostId);
        }
      } else if (isOrderTransaction(transaction)) {
        // Cancel order if transaction is cancelled
        orderService.updateOrderStatus(transaction.getOrder().getOrderId(), OrderStatus.CANCELLED);
        log.info("Transaction {} cancelled and order {} cancelled",
          transactionId, transaction.getOrder().getOrderId());
      }

      // Cancel expiration scheduling
      cancelTransactionExpiration(transactionId);

    } catch (Exception e) {
      log.error("Failed to cancel transaction {}: {}",
        transactionId, e.getMessage(), e);
    }
  }

  /**
   * Get transaction from Redis cache (if exists) or fallback to database
   * Since we only cache the transaction ID (not the full entity), we always fetch from DB
   * This avoids LazyInitializationException with lazy-loaded relationships
   */
  private Transaction getCachedTransaction(String transactionId) {
    try {
      // Check if transaction ID is still in cache (exists = not yet expired)
      Object cached = redisTemplate.opsForValue()
        .get(TRANSACTION_CACHE_PREFIX + transactionId);

      // Regardless of cache hit, fetch fresh from DB to avoid lazy loading issues
      // The cache just acts as a marker that this transaction should be checked
      return transactionRepository.findById(transactionId).orElse(null);
    } catch (Exception e) {
      log.warn("Failed to retrieve transaction {}: {}", transactionId, e.getMessage());
      return transactionRepository.findById(transactionId).orElse(null);
    }
  }
}


