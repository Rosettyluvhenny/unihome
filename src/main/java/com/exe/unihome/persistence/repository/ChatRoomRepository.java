package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.chat.ChatRoom;
import com.exe.unihome.websocket.enums.ChatRoomType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {

  /**
   * Find all chat rooms where the user is participant (either userAId or userBId)
   */
  List<ChatRoom> findByUserAIdOrUserBId(String userAId, String userBId);

  /**
   * Find private chat room between two users
   */
  List<ChatRoom> findByTypeAndUserAIdAndUserBId(ChatRoomType type, String userAId, String userBId);

  /**
   * Find bot chat room for a user
   */
  List<ChatRoom> findByTypeAndUserAId(ChatRoomType type, String userAId);
}


