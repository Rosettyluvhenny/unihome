package com.exe.unihome.chat.service;

import com.exe.unihome.persistence.entity.chat.ChatMessage;
import com.exe.unihome.persistence.entity.chat.ChatRoom;
import com.exe.unihome.websocket.enums.SenderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatService {
  Optional<ChatRoom> findRoomById(UUID roomId);

  ChatMessage saveMessage(UUID roomId, UUID senderId, String content, SenderType senderType);

  Page<ChatMessage> getMessagesByRoomId(UUID roomId, Pageable pageable);

  Optional<ChatMessage> getLastMessageByRoomId(UUID roomId);

  List<ChatRoom> getAllChatRoomsByUserId(UUID userId);

  void createChatNotification(String userId, UUID roomId, String senderId);

  ChatMessage sendPrivateMessage(UUID senderId, UUID recipientId, String content);

  ChatMessage sendBotMessage(UUID userId, String botType, String content);

  ChatMessage saveBotResponse(UUID roomId, String botContent);

  boolean hasAccessToRoom(UUID roomId, UUID userId);
}
