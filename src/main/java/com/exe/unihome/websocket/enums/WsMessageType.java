package com.exe.unihome.websocket.enums;

/**
 * WebSocket message types that define the category of message being sent/received.
 * Used to route messages to appropriate handlers.
 */
public enum WsMessageType {
  /**
   * Chat messages (user-to-user or user-to-bot)
   */
  CHAT("CHAT"),

  /**
   * Notification messages (system push notifications)
   */
  NOTIFICATION("NOTIFICATION"),

  /**
   * System messages (errors, pings, etc.)
   */
  SYSTEM("SYSTEM");

  private final String value;

  WsMessageType(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  /**
   * Convert string to enum value.
   *
   * @param value the string value
   * @return the matching enum, or null if not found
   */
  public static WsMessageType fromValue(String value) {
    for (WsMessageType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return null;
  }

  /**
   * Check if string value matches this enum.
   *
   * @param value the string value to check
   * @return true if value matches
   */
  public boolean matches(String value) {
    return this.value.equals(value);
  }
}

