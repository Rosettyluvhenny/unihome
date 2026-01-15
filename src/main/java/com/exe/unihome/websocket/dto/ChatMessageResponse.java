package com.exe.unihome.websocket.dto;

import com.exe.unihome.websocket.enums.SenderType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Response payload for a chat message received via WebSocket.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {

  /**
   * Message ID
   */
  @JsonProperty("id")
  private UUID id;

  /**
   * Chat room ID
   */
  @JsonProperty("roomId")
  private UUID roomId;

  /**
   * Sender ID (userId or null for bot)
   */
  @JsonProperty("senderId")
  private UUID senderId;

  /**
   * Sender type: USER or BOT
   */
  @JsonProperty("senderType")
  private SenderType senderType;

  /**
   * Message content
   */
  @JsonProperty("content")
  private String content;

  /**
   * Timestamp when message was created
   */
  @JsonProperty("createdAt")
  private Instant createdAt;
}

