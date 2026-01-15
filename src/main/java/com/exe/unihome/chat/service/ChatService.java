package com.exe.unihome.chat.service;

import com.exe.unihome.persistence.entity.chat.ChatMessage;
import com.exe.unihome.persistence.entity.chat.ChatRoom;
import com.exe.unihome.websocket.enums.SenderType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ChatService {
  Optional<ChatRoom> findRoomById(String roomId);

  ChatMessage saveMessage(String roomId, String senderId, String content, SenderType senderType);

  Page<ChatMessage> getMessagesByRoomId(String roomId, Pageable pageable);

  Optional<ChatMessage> getLastMessageByRoomId(String roomId);

  List<ChatRoom> getAllChatRoomsByUserId(String userId);

  void createChatNotification(String userId, String roomId, String senderId);

  ChatMessage sendPrivateMessage(String senderId, String recipientId, String content);

  ChatMessage sendBotMessage(String userId, String botType, String content);

  ChatMessage saveBotResponse(String roomId, String botContent);

  boolean hasAccessToRoom(String roomId, String userId);
}
