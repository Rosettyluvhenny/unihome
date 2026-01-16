package com.exe.unihome.chat.serviceImp;

import com.exe.unihome.chat.service.ChatService;
import com.exe.unihome.chat.service.RoomService;
import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

  private final ChatRoomRepository chatRoomRepository;
  private final ChatMessageRepository chatMessageRepository;
  private final NotificationService notificationService;
  private final RoomService roomService;
  private final ObjectMapper objectMapper;

  public Optional<ChatRoom> findRoomById(String roomId) {
    return chatRoomRepository.findById(roomId);
  }

  public ChatMessage saveMessage(String roomId, String senderId, String content, SenderType senderType) {
    ChatRoom room = roomService.findById(roomId).orElseThrow(() -> new AppException(ErrorCode.ROOM_NOT_FOUND));
    ChatMessage message = new ChatMessage();
    message.setRoomId(room.getId());
    message.setSenderId(senderId);
    message.setContent(content);
    message.setSenderType(senderType);
    message.setCreatedAt(LocalDateTime.now());
    return chatMessageRepository.save(message);
  }

  public Page<ChatMessage> getMessagesByRoomId(String roomId, Pageable pageable) {
    return chatMessageRepository.findByRoomId(roomId, pageable);
  }

  /**
   * Load messages using cursor-based pagination.
   * Validates user access to the room before returning messages.
   *
   * @param roomId   ID of the chat room
   * @param userId   ID of the user accessing the messages
   * @param before   Timestamp cursor - messages created before this time
   * @param beforeId Message ID cursor (reserved for future use)
   * @param limit    Maximum number of messages to return
   * @return List of messages ordered by creation time (newest first)
   * @throws AppException if user doesn't have access to the room
   */
  public List<ChatMessage> loadMessagesByCursor(String roomId, String userId, LocalDateTime before, String beforeId, int limit) {
    // Validate user has access to this room
    if (!hasAccessToRoom(roomId, userId)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    // Use current time if before is not specified
    LocalDateTime cursorTime = before != null ? before : LocalDateTime.now();

    // Load messages before the cursor time
    if (beforeId != null) {
      return chatMessageRepository.findMessagesBeforeCursor(roomId, cursorTime, limit);
    } else if (before != null) {
      return chatMessageRepository.findMessagesBeforeCursor(roomId, cursorTime, limit);
    } else {
      // If no cursor provided, get most recent messages
      return chatMessageRepository.findRecentMessages(roomId, limit);
    }
  }

  public Optional<ChatMessage> getLastMessageByRoomId(String roomId) {
    return chatMessageRepository.findTopByRoomIdOrderByCreatedAtDesc(roomId);
  }

  /**
   * Get all chat rooms for a user (both private and bot rooms).
   */
  public List<ChatRoom> getAllChatRoomsByUserId(String userId) {
    return chatRoomRepository.findByUserAIdOrUserBId(userId, userId);
  }

  /**
   * Create a notification for a user when they receive a chat message but are offline.
   */
  public void createChatNotification(String userId, String roomId, String senderId) {
    ObjectNode payload = objectMapper.createObjectNode();
    payload.put("action", "NEW_CHAT_MESSAGE");
    payload.put("roomId", roomId);
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
  public ChatMessage sendPrivateMessage(String senderId, String recipientId, String content) {
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
  public ChatMessage sendBotMessage(String userId, String botType, String content) {
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
  public ChatMessage saveBotResponse(String roomId, String botContent) {
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
  public boolean hasAccessToRoom(String roomId, String userId) {
    return roomService.isValidRoomForUser(roomId, userId);
  }
}



