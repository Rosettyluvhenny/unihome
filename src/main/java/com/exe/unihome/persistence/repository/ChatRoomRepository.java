package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.chat.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, UUID> {

  /**
   * Find all chat rooms where the user is participant (either userAId or userBId)
   */
  List<ChatRoom> findByUserAIdOrUserBId(UUID userAId, UUID userBId);

  /**
   * Find private chat room between two users
   */
  List<ChatRoom> findByTypeAndUserAIdAndUserBId(String type, UUID userAId, UUID userBId);

  /**
   * Find bot chat room for a user
   */
  List<ChatRoom> findByTypeAndUserAId(String type, UUID userAId);
}


