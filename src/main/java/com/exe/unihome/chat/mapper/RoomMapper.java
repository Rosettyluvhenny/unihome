package com.exe.unihome.chat.mapper;

import com.exe.unihome.chat.dto.ChatRoomResponse;
import com.exe.unihome.persistence.entity.chat.ChatRoom;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RoomMapper {

  ChatRoomResponse toResponse(ChatRoom chatRoom);
}
