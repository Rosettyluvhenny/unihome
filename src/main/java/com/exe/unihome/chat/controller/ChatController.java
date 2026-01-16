package com.exe.unihome.chat.controller;

import com.exe.unihome.chat.service.ChatService;
import com.exe.unihome.persistence.entity.chat.ChatMessage;
import com.exe.unihome.persistence.entity.chat.ChatRoom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chat")
public class ChatController {

  private final ChatService chatService;

  /**
   * Get all chat rooms for the authenticated user (both private and bot).
   */
  @GetMapping("/rooms")
  public ResponseEntity<List<ChatRoom>> getAllChatRooms() {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    List<ChatRoom> rooms = chatService.getAllChatRoomsByUserId(userId);
    return ResponseEntity.ok(rooms);
  }

  /**
   * Get paginated chat messages for a specific room.
   */
  @GetMapping("/rooms/{roomId}/messages")
  public ResponseEntity<Page<ChatMessage>> getChatMessages(
    @PathVariable String roomId,
    Pageable pageable
  ) {
    Page<ChatMessage> messages = chatService.getMessagesByRoomId(roomId, pageable);
    return ResponseEntity.ok(messages);
  }


}
