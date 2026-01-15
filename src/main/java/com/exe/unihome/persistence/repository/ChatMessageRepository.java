package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.chat.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, String> {

  Page<ChatMessage> findByRoomId(String roomId, Pageable pageable);

  Optional<ChatMessage> findTopByRoomIdOrderByCreatedAtDesc(String roomId);
}
