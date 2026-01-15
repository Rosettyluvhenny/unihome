package com.exe.unihome.persistence.entity.chat;

import com.exe.unihome.websocket.enums.ChatRoomType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_room")
@Data
public class ChatRoom {

  @Id
  private UUID id;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private ChatRoomType type;

  @Column(name = "user_a_id", nullable = false)
  private UUID userAId;

  @Column(name = "user_b_id")
  private UUID userBId;

  @Column(name = "bot_type")
  private String botType;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
