package com.exe.unihome.persistence.entity.payment;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Payment Entity
 * Represents a payment method (e.g., PayOS, etc.)
 * Acts as a reference table for transaction payment methods
 */
@Entity
@Table(name = "payment")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

  @Id
  @Column(name = "id", nullable = false, length = 50)
  private String id;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "is_active", nullable = false)
  @Builder.Default
  private Boolean isActive = Boolean.TRUE;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false, nullable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  private String img;
}

