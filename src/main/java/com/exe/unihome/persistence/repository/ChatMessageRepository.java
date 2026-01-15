package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.chat.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

  Page<ChatMessage> findByRoomId(UUID roomId, Pageable pageable);

  Optional<ChatMessage> findTopByRoomIdOrderByCreatedAtDesc(UUID roomId);
}
