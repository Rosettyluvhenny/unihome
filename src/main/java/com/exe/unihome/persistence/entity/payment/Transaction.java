package com.exe.unihome.persistence.entity.payment;

import com.exe.unihome.persistence.entity.order.Order;
import com.exe.unihome.persistence.entity.subscription.UserBoost;
import com.exe.unihome.persistence.enums.TransactionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Transaction Entity
 * Represents a payment transaction for an order
 * Tracks payment status and updates order status accordingly
 * <p>
 * Status Flow:
 * PENDING  -> SUCCESS (when payment confirmed)
 * PENDING  -> CANCEL  (when user cancels)
 * PENDING  -> EXPIRED (when expiration time reached)
 */
@Entity
@Table(name = "transaction",
  indexes = {
    @Index(name = "idx_transaction_order_id", columnList = "order_id"),
    @Index(name = "idx_transaction_payment_id", columnList = "payment_id"),
    @Index(name = "idx_transaction_status", columnList = "status"),
    @Index(name = "idx_transaction_created_at", columnList = "created_at")
  })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {

  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
  @Column(name = "id", nullable = false, columnDefinition = "VARCHAR(36)")
  private String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = true)
  private Order order;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "payment_id", nullable = false)
  private Payment payment;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_boost_id", nullable = true)
  private UserBoost userBoost;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  @Builder.Default
  private TransactionStatus status = TransactionStatus.PENDING;

  @Column(name = "url", columnDefinition = "TEXT")
  private String url;

  @Column(name = "total_price")
  private BigDecimal totalPrice;

  @Column(name = "paid_at")
  private LocalDateTime paidAt;

  @Column(name = "pay_os_code")
  private String payOsCode;

  @Column(name = "pay_os_qr")
  private String payOsQr;

  @Column(name = "expired_at")
  private LocalDateTime expiredAt;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false, nullable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}

