package com.exe.unihome.chat.serviceImp;

import com.exe.unihome.chat.service.RoomService;
import com.exe.unihome.persistence.entity.chat.ChatRoom;
import com.exe.unihome.persistence.repository.ChatRoomRepository;
import com.exe.unihome.websocket.enums.ChatRoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {

  private final ChatRoomRepository chatRoomRepository;

  /**
   * Get or create a PRIVATE chat room between two users.
   * Ensures idempotent room creation - returns existing room if it already exists.
   *
   * @param userAId First user ID
   * @param userBId Second user ID
   * @return ChatRoom (existing or newly created)
   */
  @Transactional
  public ChatRoom getOrCreatePrivateRoom(String userAId, String userBId) {
    // Normalize user IDs (always put smaller ID first) to ensure consistency
    String firstUserId = userAId.compareTo(userBId) < 0 ? userAId : userBId;
    String secondUserId = userAId.compareTo(userBId) < 0 ? userBId : userAId;

    // Try to find existing room
    List<ChatRoom> existingRooms = chatRoomRepository.findByTypeAndUserAIdAndUserBId(
      ChatRoomType.PRIVATE,
      firstUserId,
      secondUserId
    );

    if (!existingRooms.isEmpty()) {
      return existingRooms.get(0);
    }

    // Create new room if it doesn't exist
    ChatRoom newRoom = new ChatRoom();
    newRoom.setType(ChatRoomType.PRIVATE);
    newRoom.setUserAId(firstUserId);
    newRoom.setUserBId(secondUserId);
    newRoom.setCreatedAt(Instant.now());

    return chatRoomRepository.save(newRoom);
  }

  /**
   * Get or create a BOT chat room for a user.
   * Ensures idempotent room creation - returns existing room if it already exists.
   *
   * @param userId  User ID
   * @param botType Type of bot (e.g., "FURNITURE_ADVISOR", "INTERIOR_DESIGNER")
   * @return ChatRoom (existing or newly created)
   */
  @Transactional
  public ChatRoom getOrCreateBotRoom(String userId, String botType) {
    // Try to find existing room
    List<ChatRoom> existingRooms = chatRoomRepository.findByTypeAndUserAId(ChatRoomType.BOT, userId);

    Optional<ChatRoom> matchingRoom = existingRooms.stream()
      .filter(room -> botType.equals(room.getBotType()))
      .findFirst();

    if (matchingRoom.isPresent()) {
      return matchingRoom.get();
    }

    // Create new room if it doesn't exist
    ChatRoom newRoom = new ChatRoom();
    newRoom.setType(ChatRoomType.BOT);
    newRoom.setUserAId(userId);
    newRoom.setBotType(botType);
    newRoom.setCreatedAt(Instant.now());

    return chatRoomRepository.save(newRoom);
  }

  /**
   * Find a private chat room between two users (if exists).
   *
   * @param userAId First user ID
   * @param userBId Second user ID
   * @return Optional ChatRoom
   */
  public Optional<ChatRoom> findPrivateRoom(String userAId, String userBId) {
    String firstUserId = userAId.compareTo(userBId) < 0 ? userAId : userBId;
    String secondUserId = userAId.compareTo(userBId) < 0 ? userBId : userAId;

    List<ChatRoom> rooms = chatRoomRepository.findByTypeAndUserAIdAndUserBId(
      ChatRoomType.PRIVATE,
      firstUserId,
      secondUserId
    );

    return rooms.isEmpty() ? Optional.empty() : Optional.of(rooms.get(0));
  }

  /**
   * Find a bot chat room for a user (if exists).
   *
   * @param userId  User ID
   * @param botType Type of bot
   * @return Optional ChatRoom
   */
  public Optional<ChatRoom> findBotRoom(String userId, String botType) {
    List<ChatRoom> rooms = chatRoomRepository.findByTypeAndUserAId(ChatRoomType.BOT, userId);

    return rooms.stream()
      .filter(room -> botType.equals(room.getBotType()))
      .findFirst();
  }

  /**
   * Validate if a room exists and belongs to the specified users.
   * For PRIVATE rooms: validates that both users are participants.
   * For BOT rooms: validates that the user is the owner.
   *
   * @param roomId Room ID to validate
   * @param userId User ID to check
   * @return true if valid, false otherwise
   */
  public boolean isValidRoomForUser(String roomId, String userId) {
    Optional<ChatRoom> room = chatRoomRepository.findById(roomId);

    if (room.isEmpty()) {
      return false;
    }

    ChatRoom chatRoom = room.get();

    if (chatRoom.getType().isPrivate()) {
      // For private rooms, user must be either userA or userB
      return chatRoom.getUserAId().equals(userId) || chatRoom.getUserBId().equals(userId);
    } else if (chatRoom.getType().isBot()) {
      // For bot rooms, user must be the owner (userA)
      return chatRoom.getUserAId().equals(userId);
    }

    return false;
  }
}

