package com.exe.unihome.service;

import com.exe.unihome.notification.NotificationChannel;
import com.exe.unihome.notification.NotificationType;
import com.exe.unihome.notification.service.NotificationService;
import com.exe.unihome.persistence.entity.chat.ChatMessage;
import com.exe.unihome.persistence.entity.chat.ChatRoom;
import com.exe.unihome.persistence.repository.ChatMessageRepository;
import com.exe.unihome.persistence.repository.ChatRoomRepository;
import com.exe.unihome.websocket.enums.SenderType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

  private final ChatRoomRepository chatRoomRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final NotificationService notificationService;
  private final RoomService roomService;
  private final ObjectMapper objectMapper;

  public Optional<ChatRoom> findRoomById(UUID roomId) {
    return chatRoomRepository.findById(roomId);
  }

  public ChatMessage saveMessage(UUID roomId, UUID senderId, String content, SenderType senderType) {
    ChatMessage message = new ChatMessage();
    message.setId(UUID.randomUUID());
    message.setRoomId(roomId);
    message.setSenderId(senderId);
    message.setContent(content);
    message.setSenderType(senderType);
    message.setCreatedAt(Instant.now());
    return chatMessageRepository.save(message);
  }

  public Page<ChatMessage> getMessagesByRoomId(UUID roomId, Pageable pageable) {
    return chatMessageRepository.findByRoomId(roomId, pageable);
  }

  public Optional<ChatMessage> getLastMessageByRoomId(UUID roomId) {
    return chatMessageRepository.findTopByRoomIdOrderByCreatedAtDesc(roomId);
  }

  /**
   * Get all chat rooms for a user (both private and bot rooms).
   */
  public List<ChatRoom> getAllChatRoomsByUserId(UUID userId) {
    return chatRoomRepository.findByUserAIdOrUserBId(userId, userId);
  }

  /**
   * Create a notification for a user when they receive a chat message but are offline.
   */
  public void createChatNotification(String userId, UUID roomId, String senderId) {
    ObjectNode payload = objectMapper.createObjectNode();
    payload.put("action", "NEW_CHAT_MESSAGE");
    payload.put("roomId", roomId.toString());
    payload.put("senderId", senderId);

    notificationService.createNotification(
      userId,
      "New message from " + senderId,
      NotificationType.CHAT,
      NotificationChannel.WEBSOCKET
    );
  }

  /**
   * Send a message in a private chat room between two users.
   * Automatically creates the room if it doesn't exist.
   *
   * @param senderId    ID of the message sender
   * @param recipientId ID of the message recipient
   * @param content     Message content
   * @return ChatMessage (saved message with room automatically created/retrieved)
   */
  @Transactional
  public ChatMessage sendPrivateMessage(UUID senderId, UUID recipientId, String content) {
    // Get or create the private chat room
    ChatRoom room = roomService.getOrCreatePrivateRoom(senderId, recipientId);

    // Save the message
    return saveMessage(room.getId(), senderId, content, SenderType.USER);
  }

  /**
   * Send a message in a bot chat room.
   * Automatically creates the room if it doesn't exist.
   *
   * @param userId  ID of the user sending the message
   * @param botType Type of bot
   * @param content Message content
   * @return ChatMessage (saved message with room automatically created/retrieved)
   */
  @Transactional
  public ChatMessage sendBotMessage(UUID userId, String botType, String content) {
    // Get or create the bot chat room
    ChatRoom room = roomService.getOrCreateBotRoom(userId, botType);

    // Save the message
    return saveMessage(room.getId(), userId, content, SenderType.USER);
  }

  /**
   * Save a bot response in an existing chat room.
   *
   * @param roomId     ID of the chat room
   * @param botContent Bot response content
   * @return ChatMessage (saved bot message)
   */
  @Transactional
  public ChatMessage saveBotResponse(UUID roomId, String botContent) {
    // Bot messages don't have a specific sender ID, so senderId can be null
    return saveMessage(roomId, null, botContent, SenderType.BOT);
  }

  /**
   * Validate if a user has access to a chat room.
   *
   * @param roomId ID of the chat room
   * @param userId ID of the user
   * @return true if user is participant of the room, false otherwise
   */
  public boolean hasAccessToRoom(UUID roomId, UUID userId) {
    return roomService.isValidRoomForUser(roomId, userId);
  }
}



