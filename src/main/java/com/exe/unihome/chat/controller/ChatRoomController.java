package com.exe.unihome.chat.controller;

import com.exe.unihome.chat.dto.ChatRoomResponse;
import com.exe.unihome.chat.service.RoomService;
import com.exe.unihome.common.model.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/chatroom")
public class ChatRoomController {
  private final RoomService roomService;

  @PostMapping()
  public ResponseEntity<ApiResponse<ChatRoomResponse>> getOrCreateChatRoom(@RequestParam String userAId) {
    String id = SecurityContextHolder.getContext().getAuthentication().getName();
    var result = roomService.getOrCreatePrivateRoom(userAId, id);
    return ResponseEntity.ok(roomService.toApiResponse(result));
  }
  
}
