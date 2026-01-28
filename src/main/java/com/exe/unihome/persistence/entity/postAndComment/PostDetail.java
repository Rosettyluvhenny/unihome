package com.exe.unihome.persistence.entity.postAndComment;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_detail")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class PostDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  Post post;

  @Column(name = "post_id", insertable = false, updatable = false)
  String postId;
  
  @Column(columnDefinition = "TEXT")
  String description;

  @Column(columnDefinition = "TEXT")
  String image;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  LocalDateTime updatedAt;
}

