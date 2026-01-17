package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.chat.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

  Page<ChatMessage> findByRoomId(String roomId, Pageable pageable);

  Optional<ChatMessage> findTopByRoomIdOrderByCreatedAtDesc(String roomId);

  /**
   * Load messages using cursor-based pagination.
   * Fetches messages created before the given timestamp.
   * Ordered by creation time in descending order (newest first).
   *
   * @param roomId ID of the chat room
   * @param before Timestamp cursor - messages created before this time
   * @param limit  Maximum number of messages to return
   * @return List of messages
   */
  @Query(nativeQuery = true, value = "SELECT * FROM unihome.chat_message WHERE CAST(room_id AS VARCHAR) = :roomId AND created_at < :before ORDER BY created_at DESC LIMIT :limit")
  List<ChatMessage> findMessagesBeforeCursor(
    @Param("roomId") String roomId,
    @Param("before") LocalDateTime before,
    @Param("limit") int limit
  );

  /**
   * Load all messages for a room without cursor.
   *
   * @param roomId ID of the chat room
   * @param limit  Maximum number of messages to return
   * @return List of most recent messages
   */
  @Query(nativeQuery = true, value = "SELECT * FROM unihome.chat_message WHERE CAST(room_id AS VARCHAR) = :roomId ORDER BY created_at DESC LIMIT :limit")
  List<ChatMessage> findRecentMessages(
    @Param("roomId") String roomId,
    @Param("limit") int limit
  );
}
