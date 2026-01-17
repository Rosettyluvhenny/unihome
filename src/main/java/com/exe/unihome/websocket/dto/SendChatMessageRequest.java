package com.exe.unihome.websocket.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Payload for sending a chat message via WebSocket.
 * Logic determines the room automatically:
 * - For user-to-user: set recipientId (room is auto-created/retrieved)
 * - For user-to-bot: set botType (room is auto-created/retrieved)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendChatMessageRequest {

  /**
   * Recipient user ID (for private 1-to-1 chat).
   * If set, botType should be null.
   */
  @JsonProperty("recipientId")
  private String recipientId;

  /**
   * Bot type (for user-to-bot chat).
   * If set, recipientId should be null.
   * Example: "SUPPORT", "FURNITURE_ADVISOR"
   */
  @JsonProperty("botType")
  private String botType;

  /**
   * Message content
   */
  @JsonProperty("content")
  private String content;
}

