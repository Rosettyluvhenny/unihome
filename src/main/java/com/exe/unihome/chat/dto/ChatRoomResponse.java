package com.exe.unihome.chat.dto;

import com.exe.unihome.auth.model.UserSummary;
import com.exe.unihome.websocket.enums.ChatRoomType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
public class ChatRoomResponse {
  private String id;

  private ChatRoomType type;

  private String botType;

  private LocalDateTime createdAt;


  private List<UserSummary> participant = new ArrayList<>();
}
