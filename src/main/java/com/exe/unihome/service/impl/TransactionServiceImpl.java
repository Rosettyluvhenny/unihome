package com.exe.unihome.service.impl;

import com.exe.unihome.persistence.entity.order.Order;
import com.exe.unihome.persistence.entity.payment.Payment;
import com.exe.unihome.persistence.entity.payment.Transaction;
import com.exe.unihome.persistence.entity.subscription.UserBoost;
import com.exe.unihome.persistence.enums.OrderStatus;
import com.exe.unihome.persistence.enums.TransactionStatus;
import com.exe.unihome.persistence.repository.OrderRepository;
import com.exe.unihome.persistence.repository.PaymentRepository;
import com.exe.unihome.persistence.repository.TransactionRepository;
import com.exe.unihome.persistence.repository.UserBoostRepository;
import com.exe.unihome.service.OrderService;
import com.exe.unihome.service.TransactionService;
import com.exe.unihome.service.model.TransactionCancelledEvent;
import com.exe.unihome.service.model.TransactionCreatedEvent;
import com.exe.unihome.service.model.TransactionSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Transaction Service Implementation
 * Handles transaction creation, confirmation, and cancellation
 * Publishes events for async processing of expiration and order status updates
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionServiceImpl implements TransactionService {
  @Value("${payment.minutes}")
  private long EXPIRATION_MINUTES;
  private final TransactionRepository transactionRepository;
  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final OrderService orderService;
  private final TransactionExpirationService transactionExpirationService;
  private final ApplicationEventPublisher eventPublisher;
  private final Clock clock;
  private final UserBoostRepository userBoostRepository;

  /**
   * Create a new transaction for an order
   * Publishes TransactionCreatedEvent to trigger automatic expiration scheduling
   */
  @Override
  @Transactional
  public Transaction createOrderTransaction(UUID orderId, String paymentMethodId,
                                            String paymentUrl) {
    try {
      // Fetch order and payment method
      Order order = orderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));

      Payment payment = paymentRepository.findById(paymentMethodId)
        .orElseThrow(() -> new IllegalArgumentException("Payment method not found: " + paymentMethodId));

      // Create transaction with PENDING status
      Transaction transaction = Transaction.builder()
        .order(order)
        .payment(payment)
        .status(TransactionStatus.PENDING)
        .url(paymentUrl)
        .expiredAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
        .build();

      // Save transaction
      Transaction savedTransaction = transactionRepository.save(transaction);
      log.info("Created transaction {} for order {} with payment method {}",
        savedTransaction.getId(), orderId, paymentMethodId);

      // Publish event to schedule automatic expiration
      TransactionCreatedEvent event = TransactionCreatedEvent.builder()
        .transactionId(savedTransaction.getId())
        .orderId(orderId)
        .createdAt(savedTransaction.getCreatedAt())
        .expirationTime(savedTransaction.getExpiredAt())
        .build();

      eventPublisher.publishEvent(event);
      log.info("Published TransactionCreatedEvent for transaction {}", savedTransaction.getId());

      return savedTransaction;

    } catch (Exception e) {
      log.error("Error creating transaction for order {}: {}", orderId, e.getMessage(), e);
      throw new RuntimeException("Failed to create transaction", e);
    }
  }

  @Override
  public Transaction createBoostTransaction(String userBoostId, String paymentUrl) {
    try {
      // Fetch order and payment method
      UserBoost userBoost = userBoostRepository.findById(userBoostId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + userBoostId));

      Payment payment = paymentRepository.findByName("ONLINE")
        .orElseThrow(() -> new IllegalArgumentException("Payment method not found: ONLINE"));

      // Create transaction with PENDING status
      Transaction transaction = Transaction.builder()
        .userBoost(userBoost)
        .payment(payment)
        .status(TransactionStatus.PENDING)
        .url(paymentUrl)
        .expiredAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
        .build();

      // Save transaction
      Transaction savedTransaction = transactionRepository.save(transaction);
      log.info("Created transaction {} for order {} with payment method {}",
        savedTransaction.getId(), userBoostId, "ONLINE");

      // Publish event to schedule automatic expiration
      TransactionCreatedEvent event = TransactionCreatedEvent.builder()
        .transactionId(savedTransaction.getId())
        .userBoostId(userBoostId)
        .createdAt(savedTransaction.getCreatedAt())
        .expirationTime(savedTransaction.getExpiredAt())
        .build();

      eventPublisher.publishEvent(event);
      log.info("Published TransactionCreatedEvent for transaction {}", savedTransaction.getId());

      return savedTransaction;

    } catch (Exception e) {
      log.error("Error creating transaction for order {}: {}", userBoostId, e.getMessage(), e);
      throw new RuntimeException("Failed to create transaction", e);
    }
  }

  /**
   * Confirm transaction payment
   * Updates transaction status to SUCCESS and order status to SHIPPING
   */
  @Override
  @Transactional
  public void confirmTransaction(String transactionId) {
    try {
      Transaction transaction = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

      // Only allow confirmation if transaction is PENDING
      if (transaction.getStatus() != TransactionStatus.PENDING) {
        throw new IllegalStateException("Transaction is not pending: " + transaction.getStatus());
      }

      // Update transaction status to SUCCESS
      transaction.setStatus(TransactionStatus.SUCCESS);
      transaction.setPaidAt(LocalDateTime.now(clock));
      transactionRepository.save(transaction);
      log.info("Confirmed transaction {} with status SUCCESS", transactionId);

      // Update order status to SHIPPING
      orderService.updateOrderStatus(transaction.getOrder().getOrderId(), OrderStatus.SHIPPING);

      // Publish success event to cancel scheduled expiration
      eventPublisher.publishEvent(new TransactionSuccessEvent(transactionId));
      log.info("Published TransactionSuccessEvent for transaction {}", transactionId);

    } catch (Exception e) {
      log.error("Error confirming transaction {}: {}", transactionId, e.getMessage(), e);
      throw new RuntimeException("Failed to confirm transaction", e);
    }
  }

  /**
   * Cancel a transaction
   * Updates transaction status to CANCEL and order status to CANCELLED
   */
  @Override
  @Transactional
  public void cancelTransaction(String transactionId, String reason) {
    try {
      Transaction transaction = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

      // Only allow cancellation if transaction is PENDING
      if (transaction.getStatus() != TransactionStatus.PENDING) {
        throw new IllegalStateException("Cannot cancel non-pending transaction: " + transaction.getStatus());
      }

      // Update transaction status to CANCEL
      transaction.setStatus(TransactionStatus.CANCEL);
      transactionRepository.save(transaction);
      log.info("Cancelled transaction {} with reason: {}", transactionId, reason);

      // Update order status to CANCELLED
      orderService.updateOrderStatus(transaction.getOrder().getOrderId(), OrderStatus.CANCELLED);

      // Publish cancellation event
      TransactionCancelledEvent event = TransactionCancelledEvent.builder()
        .transactionId(transactionId)
        .orderId(transaction.getOrder().getOrderId())
        .timestamp(LocalDateTime.now(clock))
        .reason(reason)
        .automatic(false)  // Manual cancellation
        .build();

      eventPublisher.publishEvent(event);
      log.info("Published TransactionCancelledEvent for transaction {}", transactionId);

    } catch (Exception e) {
      log.error("Error cancelling transaction {}: {}", transactionId, e.getMessage(), e);
      throw new RuntimeException("Failed to cancel transaction", e);
    }
  }

  /**
   * Get transaction by ID
   */
  @Override
  public Optional<Transaction> getTransaction(String transactionId) {
    return transactionRepository.findById(transactionId);
  }

  /**
   * Get transaction by order ID
   */
  @Override
  public Optional<Transaction> getTransactionByOrderId(UUID orderId) {
    return transactionRepository.findByOrder_OrderId(orderId);
  }

  /**
   * Update transaction status
   */
  @Override
  @Transactional
  public void updateTransactionStatus(String transactionId, TransactionStatus status) {
    try {
      Transaction transaction = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

      transaction.setStatus(status);
      transactionRepository.save(transaction);
      log.info("Updated transaction {} status to {}", transactionId, status);

    } catch (Exception e) {
      log.error("Error updating transaction {} status: {}", transactionId, e.getMessage(), e);
      throw new RuntimeException("Failed to update transaction status", e);
    }
  }

  /**
   * Check if transaction is active (PENDING and not expired)
   */
  @Override
  public boolean isTransactionActive(String transactionId) {
    try {
      Optional<Transaction> transaction = transactionRepository.findById(transactionId);

      if (transaction.isEmpty()) {
        return false;
      }

      Transaction t = transaction.get();

      // Transaction is active if status is PENDING and not expired
      return t.getStatus() == TransactionStatus.PENDING &&
        (t.getExpiredAt() == null || t.getExpiredAt().isAfter(LocalDateTime.now(clock)));

    } catch (Exception e) {
      log.error("Error checking transaction activity for {}: {}", transactionId, e.getMessage(), e);
      return false;
    }
  }
}

