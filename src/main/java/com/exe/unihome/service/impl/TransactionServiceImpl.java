package com.exe.unihome.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.CreatePaymentLinkRequestBody;
import com.exe.unihome.persistence.entity.order.Order;
import com.exe.unihome.persistence.entity.payment.Payment;
import com.exe.unihome.persistence.entity.payment.Transaction;
import com.exe.unihome.persistence.entity.subscription.UserBoost;
import com.exe.unihome.persistence.entity.subscription.UserBoostStatus;
import com.exe.unihome.persistence.enums.OrderStatus;
import com.exe.unihome.persistence.enums.TransactionStatus;
import com.exe.unihome.persistence.repository.OrderRepository;
import com.exe.unihome.persistence.repository.PaymentRepository;
import com.exe.unihome.persistence.repository.TransactionRepository;
import com.exe.unihome.persistence.repository.UserBoostRepository;
import com.exe.unihome.postAndSubscription.UserBoostService;
import com.exe.unihome.service.OrderService;
import com.exe.unihome.service.TransactionService;
import com.exe.unihome.service.model.TransactionCancelledEvent;
import com.exe.unihome.service.model.TransactionCreatedEvent;
import com.exe.unihome.service.model.TransactionSuccessEvent;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
  private final UserBoostService userBoostService;
  @Value("${payment.minutes}")
  private long EXPIRATION_MINUTES;
  private final TransactionRepository transactionRepository;
  private final PaymentRepository paymentRepository;
  private final OrderRepository orderRepository;
  private final OrderService orderService;
  private final TransactionExpirationService transactionExpirationService;
  private final ApplicationEventPublisher eventPublisher;
  private final PayOsOrderServiceImpl payOsService;
  private final Clock clock;
  private final UserBoostRepository userBoostRepository;

  @Value("${payos.returnUrl}")
  private String returnUrl;
  @Value("${payos.cancelUrl}")
  private String cancelUrl;

  /**
   * Create a new transaction for an order
   * Publishes TransactionCreatedEvent to trigger automatic expiration scheduling
   */
  @Override
  @Transactional
  public Transaction createOrderTransaction(UUID orderId, String paymentMethodId) {
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
        .totalPrice(order.getTotalPrice())
        .status(TransactionStatus.PENDING)
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
      savedTransaction = transactionRepository.save(transaction);

      return savedTransaction;

    } catch (Exception e) {
      log.error("Error creating transaction for order {}: {}", orderId, e.getMessage(), e);
      throw new AppException(ErrorCode.INVALID_TRANSACTION);
    }
  }

  @Override
  @Transactional
  public Transaction createBoostTransaction(String userBoostId) {
    try {
      // Fetch order and payment method
      UserBoost userBoost = userBoostRepository.findById(userBoostId)
        .orElseThrow(() -> new AppException(ErrorCode.USER_BOOST_NOT_FOUND));

      Payment payment = paymentRepository.findByIdAndIsActiveTrue("ONLINE")
        .orElseThrow(() -> new AppException(ErrorCode.PAYMENT_NOT_FOUND));
      // Create transaction with PENDING status

      if (!userBoost.getStatus().equals(UserBoostStatus.PENDING)) {
        throw new AppException(ErrorCode.INVALID_TRANSACTION);
      }
      Transaction transaction = Transaction.builder()
        .userBoost(userBoost)
        .payment(payment)
        .status(TransactionStatus.PENDING)
        .totalPrice(userBoost.getPrice())
        .expiredAt(LocalDateTime.now().plusMinutes(EXPIRATION_MINUTES))
        .build();

      // Save transaction
      Transaction savedTransaction = transactionRepository.save(transaction);
      log.info("Created transaction {} for order {} with payment method {}",
        savedTransaction.getId(), userBoostId, "ONLINE");
      createPaymentLink(savedTransaction);
      // Publish event to schedule automatic expiration
      TransactionCreatedEvent event = TransactionCreatedEvent.builder()
        .transactionId(savedTransaction.getId())
        .userBoostId(userBoostId)
        .createdAt(savedTransaction.getCreatedAt())
        .expirationTime(savedTransaction.getExpiredAt())
        .build();

      eventPublisher.publishEvent(event);
      log.info("Published TransactionCreatedEvent for transaction {}", savedTransaction.getId());
      savedTransaction = transactionRepository.save(savedTransaction);
      return savedTransaction;

    } catch (Exception e) {
      log.error("Error creating transaction for order {}: {}", userBoostId, e.getMessage(), e);
      throw new AppException(ErrorCode.INVALID_TRANSACTION);
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
      Transaction transaction = transactionRepository.findByPayOsCode(transactionId)
        .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

      // Only allow confirmation if transaction is PENDING
      if (transaction.getStatus() != TransactionStatus.PENDING) {
        throw new AppException(ErrorCode.TRANSACTION_NOT_PENDING);
      }

      // Update transaction status to SUCCESS
      transaction.setStatus(TransactionStatus.SUCCESS);
      transaction.setPaidAt(LocalDateTime.now(clock));
      transactionRepository.save(transaction);
      log.info("Confirmed transaction {} with status SUCCESS", transactionId);

      // Update order status to SHIPPING

      if (getOrderId(transaction) != null)
        orderService.updateOrderStatus(transaction.getOrder().getOrderId(), OrderStatus.SHIPPING);
      else
        userBoostService.updateUserBoostStatus(transaction.getUserBoost().getId(), UserBoostStatus.ACTIVE);

      // Publish success event to cancel scheduled expiration
      eventPublisher.publishEvent(new TransactionSuccessEvent(transactionId));
      log.info("Published TransactionSuccessEvent for transaction {}", transactionId);

    } catch (Exception e) {
      log.error("Error confirming transaction {}: {}", transactionId, e.getMessage(), e);
      throw new AppException(ErrorCode.INVALID_TRANSACTION);
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
        .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

      // Only allow cancellation if transaction is PENDING
      if (transaction.getStatus() != TransactionStatus.PENDING) {
        throw new IllegalStateException("Cannot cancel non-pending transaction: " + transaction.getStatus());
      }

      // Update transaction status to CANCEL
      transaction.setStatus(TransactionStatus.CANCEL);
      transactionRepository.save(transaction);
      log.info("Cancelled transaction {} with reason: {}", transactionId, reason);
      // Update order status to CANCELLED

      if (getOrderId(transaction) != null)
        orderService.updateOrderStatus(transaction.getOrder().getOrderId(), OrderStatus.CANCELLED);
      else
        userBoostService.updateUserBoostStatus(transaction.getUserBoost().getId(), UserBoostStatus.CANCELLED);

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
      throw new AppException(ErrorCode.INVALID_TRANSACTION);
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
        .orElseThrow(() -> new AppException(ErrorCode.TRANSACTION_NOT_FOUND));

      transaction.setStatus(status);
      transactionRepository.save(transaction);
      log.info("Updated transaction {} status to {}", transactionId, status);

    } catch (Exception e) {
      log.error("Error updating transaction {} status: {}", transactionId, e.getMessage(), e);
      throw new RuntimeException("Failed to update transaction status", e);
    }
  }

  private void createPaymentLink(Transaction savedTransaction) {
    try {
      long expiredEpoch = savedTransaction.getExpiredAt()
        .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
        .toEpochSecond();
      log.info("return {} , cancel {}", this.returnUrl, this.cancelUrl);
      CreatePaymentLinkRequestBody paymentRequest =
        new CreatePaymentLinkRequestBody(
          "Unihome order#" + savedTransaction.getId().substring(0, 10),
          "Unihome order#" + savedTransaction.getId().substring(0, 10),
          this.returnUrl,
          savedTransaction.getTotalPrice().intValue(), // Convert to int for payment
          this.cancelUrl,
          expiredEpoch
        );

      ObjectNode paymentResult = payOsService.createPaymentLink(paymentRequest);
      log.info("check {}", paymentResult != null);
      log.info("check2  {}", paymentResult.get("error").asInt());

      if (paymentResult != null && paymentResult.get("error").asInt() == 0) {
        if (paymentResult.has("data") && paymentResult.get("data").has("orderCode")) {
          String orderCode = paymentResult.get("data").get("orderCode").asText();
          savedTransaction.setPayOsCode(orderCode);
          String payOsLink = paymentResult.get("data").get("checkoutUrl").asText();
          log.info("Payment link created successfully:" + payOsLink);
          savedTransaction.setUrl(payOsLink);
          String qrCode = paymentResult.get("data").get("qrCode").asText();
          savedTransaction.setPayOsQr(qrCode);
        }
      }
    } catch (Exception e) {
      throw new AppException(ErrorCode.PAYMENT_LINK_CREATION_FAILED);
    }

    // Publish BookingCreatedEvent for automatic cancellation scheduling
    LocalDateTime expirationTime = savedTransaction.getExpiredAt();

    TransactionCreatedEvent createdEvent =
      new TransactionCreatedEvent(
        savedTransaction.getId(), getOrderId(savedTransaction), getUserBoostId(savedTransaction), null, expirationTime);
    eventPublisher.publishEvent(createdEvent);
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

  private UUID getOrderId(Transaction savedTransaction) {
    return savedTransaction.getOrder() == null ? null : savedTransaction.getOrder().getOrderId();
  }

  private String getUserBoostId(Transaction savedTransaction) {
    return savedTransaction.getUserBoost() == null ? null : savedTransaction.getUserBoost().getId();
  }

}

