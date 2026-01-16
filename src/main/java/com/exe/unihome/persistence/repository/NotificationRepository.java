package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, String> {

  Page<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(String userId, Pageable pageable);

  Page<Notification> findAllByUserId(String userId, Pageable pageable);
}
