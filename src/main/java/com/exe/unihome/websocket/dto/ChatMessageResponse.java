package com.exe.unihome.websocket.dto;

import com.exe.unihome.websocket.enums.SenderType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

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
  private String id;

  /**
   * Chat room ID
   */
  @JsonProperty("roomId")
  private String roomId;

  /**
   * Sender ID (userId or null for bot)
   */
  @JsonProperty("senderId")
  private String senderId;

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

