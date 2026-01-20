package com.exe.unihome.chat.service;

import com.exe.unihome.chat.dto.ChatRoomResponse;
import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.persistence.entity.chat.ChatRoom;

import java.util.Optional;

public interface RoomService {
  Optional<ChatRoom> findBotRoom(String userId, String botType);

  Optional<ChatRoom> findPrivateRoom(String userAId, String userBId);

  ChatRoom getOrCreateBotRoom(String userId, String botType);

  ChatRoom getOrCreatePrivateRoom(String userAId, String userBId);

  boolean isValidRoomForUser(String roomId, String userId);

  Optional<ChatRoom> findById(String roomId);

  ApiResponse<ChatRoomResponse> toApiResponse(ChatRoom room);
}
