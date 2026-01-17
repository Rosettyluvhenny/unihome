package com.exe.unihome.websocket.dto;

import com.exe.unihome.websocket.enums.WsMessageAction;
import com.exe.unihome.websocket.enums.WsMessageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified WebSocket message envelope for both chat and notification.
 * All incoming and outgoing messages must use this structure.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WsMessage {

  /**
   * Message type: CHAT, NOTIFICATION, or SYSTEM
   */
  @JsonProperty("type")
  private WsMessageType type;

  /**
   * Action name (e.g., SEND_MESSAGE, MARK_READ)
   */
  @JsonProperty("action")
  private WsMessageAction action;

  /**
   * Payload containing the actual data
   */
  @JsonProperty("payload")
  private Object payload;
}

