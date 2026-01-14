package com.exe.unihome.notification.service;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.notification.NotificationChannel;
import com.exe.unihome.notification.NotificationRequest;
import com.exe.unihome.notification.NotificationType;
import com.exe.unihome.persistence.entity.notification.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
  Notification createNotification(String userId, String context, NotificationType type, NotificationChannel channel);

  ApiResponse<Page<Notification>> getAllNotificationsByUserIdAndUnread(Pageable pageable);

  ApiResponse<Page<Notification>> getLatestNotificationsByUserIdAndUnread(Pageable pageable);

  ApiResponse<Notification> createforTest(NotificationRequest request);
}
