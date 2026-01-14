package com.exe.unihome.persistence.entity.notification;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notification")
@Data
public class Notification {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(nullable = false)
  private String type;

  @Column(nullable = false)
  private String title;

  @Type(JsonType.class)
  @Column(columnDefinition = "jsonb")
  private JsonNode payload;

  @Column(nullable = false)
  private String channel = "IN_APP";

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Column(name = "read")
  private Boolean read;

}
