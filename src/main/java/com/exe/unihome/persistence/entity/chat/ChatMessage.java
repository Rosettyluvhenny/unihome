package com.exe.unihome.persistence.entity.chat;

import com.exe.unihome.websocket.enums.SenderType;
import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Table(name = "chat_message")
@Data
public class ChatMessage {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private String id;

  @Column(name = "room_id", nullable = false)
  private String roomId;

  @Column(name = "sender_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private SenderType senderType;

  @Column(name = "sender_id")
  private String senderId;

  @Column(nullable = false)
  private String content;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;
}
