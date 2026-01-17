package com.exe.unihome.websocket.enums;

/**
 * Chat room types that define the category of chat room.
 * Used for room creation and message handling logic.
 */
public enum ChatRoomType {
  /**
   * Private chat room between two users
   */
  PRIVATE("PRIVATE"),

  /**
   * Chat room with a bot assistant
   */
  BOT("BOT");

  private final String value;

  ChatRoomType(String value) {
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
  public static ChatRoomType fromValue(String value) {
    for (ChatRoomType type : values()) {
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
   * Check if this is a private room.
   */
  public boolean isPrivate() {
    return this == PRIVATE;
  }

  /**
   * Check if this is a bot room.
   */
  public boolean isBot() {
    return this == BOT;
  }
}

