package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, String> {
  List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
}
