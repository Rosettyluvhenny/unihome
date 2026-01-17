package com.exe.unihome.chat.mapper;

import com.exe.unihome.persistence.entity.chat.ChatMessage;
import com.exe.unihome.websocket.dto.ChatMessageResponse;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

  List<ChatMessageResponse> toResponseList(List<ChatMessage> messages);
}
