package com.exe.unihome.persistence.entity.chat;

import com.exe.unihome.websocket.enums.SenderType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "chat_message")
@Data
public class ChatMessage {

  @Id
  private UUID id;

  @Column(name = "room_id", nullable = false)
  private UUID roomId;

  @Column(name = "sender_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private SenderType senderType;

  @Column(name = "sender_id")
  private UUID senderId;

  @Column(nullable = false)
  private String content;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "read", nullable = false)
  private boolean read;
}
