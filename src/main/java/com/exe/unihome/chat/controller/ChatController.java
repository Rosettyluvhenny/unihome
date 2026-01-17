package com.exe.unihome.chat.controller;

import com.exe.unihome.chat.service.ChatService;
import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.persistence.entity.chat.ChatMessage;
import com.exe.unihome.persistence.entity.chat.ChatRoom;
import com.exe.unihome.websocket.dto.ChatMessageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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

  /**
   * Load messages using cursor-based pagination.
   * Supports loading messages before a specific timestamp and message ID.
   * <p>
   * Example:
   * GET /chat/rooms/{roomId}/messages?limit=20
   * GET /chat/rooms/{roomId}/messages?before=2024-01-16T10:30:00&beforeId=abc123&limit=20
   *
   * @param roomId ID of the chat room
   * @param before Timestamp cursor (ISO-8601 format) - messages created before this time
   * @param limit  Maximum number of messages to return (default: 20, max: 100)
   * @return List of messages ordered by creation time (newest first)
   */
  @GetMapping("/rooms/{roomId}/load-messages")
  public ResponseEntity<ApiResponse<List<ChatMessageResponse>>> loadMessages(
    @PathVariable String roomId,
    @RequestParam(required = false) LocalDateTime before,
    @RequestParam(defaultValue = "20") int limit
  ) {
    // Limit maximum to prevent abuse
    if (limit > 100) {
      limit = 100;
    }

    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    List<ChatMessage> messages = chatService.loadMessagesByCursor(roomId, userId, before, limit);
    ApiResponse<List<ChatMessageResponse>> response = chatService.toResponse(messages, roomId);
    return ResponseEntity.ok(response);
  }
}
