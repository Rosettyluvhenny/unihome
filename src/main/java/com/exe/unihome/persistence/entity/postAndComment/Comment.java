package com.exe.unihome.persistence.entity.postAndComment;

import com.exe.unihome.persistence.entity.identityAndAuth.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment")
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Comment {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  String id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "post_id", nullable = false)
  Post post;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  User user;

  @Column(columnDefinition = "TEXT")
  String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "reply_id", nullable = true)
  Comment reply;

  @Column(name = "reply_id", insertable = false, updatable = false)
  String replyId;

  @OneToMany(mappedBy = "reply", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
  @Builder.Default
  List<Comment> replies = new ArrayList<>();

  @CreatedDate
  @Column(name = "created_at", updatable = false)
  LocalDateTime createdAt;

  @LastModifiedDate
  @Column(name = "updated_at")
  LocalDateTime updatedAt;

}

