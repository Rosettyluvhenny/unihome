package com.exe.unihome.persistence.entity.chat;

import com.exe.unihome.websocket.enums.ChatRoomType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "chat_room")
@Data
public class ChatRoom {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private ChatRoomType type;

  @Column(name = "user_a_id", nullable = false)
  private String userAId;

  @Column(name = "user_b_id")
  private String userBId;

  @Column(name = "bot_type")
  private String botType;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
