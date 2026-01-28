package com.exe.unihome.persistence.entity.postAndComment;

import com.exe.unihome.persistence.entity.Category;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  String id;

  @Column(nullable = false, length = 100)
  String title;

  @Column(nullable = false, precision = 12, scale = 2)
  BigDecimal price;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  PostStatus status;

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  LocalDateTime updatedAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  User user;

  @Column(name = "user_id", insertable = false, updatable = false)
  String userId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id", nullable = false)
  Category category;

  @OneToMany(mappedBy = "post", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval = true)
  List<PostDetail> postDetail;

  @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  List<Comment> comments = new ArrayList<>();

  @OneToMany(mappedBy = "post", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  List<PostUserBoost> postUserBoosts = new ArrayList<>();
}

