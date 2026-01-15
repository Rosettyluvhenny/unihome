package com.exe.unihome.websocket.enums;

/**
 * Sender types that define who sent a chat message.
 * Used in chat messages to differentiate between user and bot messages.
 */
public enum SenderType {
  /**
   * Message sent by a user
   */
  USER("USER"),

  /**
   * Message sent by a bot
   */
  BOT("BOT");

  private final String value;

  SenderType(String value) {
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
  public static SenderType fromValue(String value) {
    for (SenderType type : values()) {
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

  /**
   * Check if this is a user message.
   */
  public boolean isUser() {
    return this == USER;
  }

  /**
   * Check if this is a bot message.
   */
  public boolean isBot() {
    return this == BOT;
  }
}

