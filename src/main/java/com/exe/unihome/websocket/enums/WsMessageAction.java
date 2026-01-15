package com.exe.unihome.websocket.enums;

/**
 * WebSocket message actions that define specific operations within a message type.
 * Used for routing and handling different actions within chat and notification flows.
 */
public enum WsMessageAction {
  // Chat Actions
  SEND_MESSAGE("SEND_MESSAGE"),
  MESSAGE_RECEIVED("MESSAGE_RECEIVED"),
  MESSAGE_SENT("MESSAGE_SENT"),
  MESSAGE_READ("MESSAGE_READ"),
  // Notification Actions
  NEW_NOTIFICATION("NEW_NOTIFICATION"),
  MARK_READ("MARK_READ"),
  NOTIFICATIONS_LIST("NOTIFICATIONS_LIST"),
  NOTIFICATION_READ("NOTIFICATION_READ"),

  // System Actions
  ERROR("ERROR"),
  PING("PING"),
  PONG("PONG");

  private final String value;

  WsMessageAction(String value) {
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
  public static WsMessageAction fromValue(String value) {
    for (WsMessageAction action : values()) {
      if (action.value.equals(value)) {
        return action;
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

  /**
   * Check if this is a chat action.
   */
  public boolean isChatAction() {
    return this == SEND_MESSAGE || this == MESSAGE_RECEIVED || this == MESSAGE_SENT;
  }

  /**
   * Check if this is a notification action.
   */
  public boolean isNotificationAction() {
    return this == NEW_NOTIFICATION || this == MARK_READ || this == NOTIFICATIONS_LIST || this == NOTIFICATION_READ;
  }

  /**
   * Check if this is a system action.
   */
  public boolean isSystemAction() {
    return this == ERROR || this == PING || this == PONG;
  }
}

