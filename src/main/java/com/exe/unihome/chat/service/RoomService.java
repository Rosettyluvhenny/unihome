package com.exe.unihome.chat.service;

import com.exe.unihome.persistence.entity.chat.ChatRoom;

import java.util.Optional;
import java.util.UUID;

public interface RoomService {
  Optional<ChatRoom> findBotRoom(UUID userId, String botType);

  Optional<ChatRoom> findPrivateRoom(UUID userAId, UUID userBId);

  ChatRoom getOrCreateBotRoom(UUID userId, String botType);

  ChatRoom getOrCreatePrivateRoom(UUID userAId, UUID userBId);

  boolean isValidRoomForUser(UUID roomId, UUID userId);
}
