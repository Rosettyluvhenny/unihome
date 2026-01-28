package com.exe.unihome.persistence.entity.postAndComment;

import com.exe.unihome.persistence.entity.subscription.UserBoost;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_user_boost")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PostUserBoost {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_boost_id", nullable = false)
  UserBoost userBoost;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  Post post;

  @Column(name = "start_time", nullable = false)
  LocalDateTime startTime;

  @Column(name = "end_time", nullable = false)
  LocalDateTime endTime;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  PostUserBoostStatus status;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  LocalDateTime updatedAt;
}

