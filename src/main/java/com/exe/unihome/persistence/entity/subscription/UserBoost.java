package com.exe.unihome.persistence.entity.subscription;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_boost")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Builder
public class UserBoost {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  String id;

  String userId;

  BigDecimal price;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  LocalDateTime updatedAt;

  @Enumerated(EnumType.STRING)
  UserBoostStatus status;

  int timeRemain = 0;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "boost_id", nullable = false)
  Boost boost;
}
