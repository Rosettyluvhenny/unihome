package com.exe.unihome.chat.service;

import com.exe.unihome.persistence.entity.chat.ChatMessage;
import com.exe.unihome.persistence.entity.chat.ChatRoom;
import com.exe.unihome.websocket.enums.SenderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ChatService {
  Optional<ChatRoom> findRoomById(String roomId);

  ChatMessage saveMessage(String roomId, String senderId, String content, SenderType senderType);

  Page<ChatMessage> getMessagesByRoomId(String roomId, Pageable pageable);

  /**
   * Load messages using cursor-based pagination.
   * Fetches messages created before the given timestamp.
   *
   * @param roomId   ID of the chat room
   * @param userId   ID of the user accessing the messages (for access validation)
   * @param before   Timestamp cursor - messages created before this time (optional, defaults to now)
   * @param beforeId Message ID cursor (optional, for additional filtering)
   * @param limit    Maximum number of messages to return
   * @return List of messages
   */
  List<ChatMessage> loadMessagesByCursor(String roomId, String userId, Instant before, String beforeId, int limit);

  Optional<ChatMessage> getLastMessageByRoomId(String roomId);

  List<ChatRoom> getAllChatRoomsByUserId(String userId);

  void createChatNotification(String userId, String roomId, String senderId);

  ChatMessage sendPrivateMessage(String senderId, String recipientId, String content);

  ChatMessage sendBotMessage(String userId, String botType, String content);

  ChatMessage saveBotResponse(String roomId, String botContent);

  boolean hasAccessToRoom(String roomId, String userId);
}
