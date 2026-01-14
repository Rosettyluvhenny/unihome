package com.exe.unihome.notification.service;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.notification.NotificationChannel;
import com.exe.unihome.notification.NotificationRequest;
import com.exe.unihome.notification.NotificationType;
import com.exe.unihome.notification.publisher.WebSocketNotificationPublisher;
import com.exe.unihome.persistence.entity.notification.Notification;
import com.exe.unihome.persistence.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository repository;
  private final WebSocketNotificationPublisher wsPublisher;
  private final ObjectMapper objectMapper;

  @Transactional
  public Notification createNotification(String userId, String title, NotificationType type, NotificationChannel channel) {
    Notification n = new Notification();
    n.setUserId(userId);
    n.setChannel(channel.toString());
    n.setType("VERIFY_EMAIL_SUCCESS");
    n.setTitle(title);
    n.setPayload(createPayload());
    n.setRead(false);

    repository.save(n);

    // push realtime nếu user online
    wsPublisher.push(n);
    return n;
  }

  private ObjectNode createPayload() {
    ObjectNode root = objectMapper.createObjectNode();
    root.put("action", NotificationType.PROFILE.toString());
    return root;
  }

  public ApiResponse<Page<Notification>> getAllNotificationsByUserIdAndUnread(Pageable pageable) {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    return ApiResponse.<Page<Notification>>builder()
      .message("Message loaded")
      .code(200)
      .data(repository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId, pageable))
      .build();
  }

  public ApiResponse<Page<Notification>> getLatestNotificationsByUserIdAndUnread(Pageable pageable) {
    String userId = SecurityContextHolder.getContext().getAuthentication().getName();
    return ApiResponse.<Page<Notification>>builder()
      .message("Message loaded")
      .code(200)
      .data(repository.findTopByUserIdAndReadFalseOrderByCreatedAtDesc(userId, pageable))
      .build();
  }

  @Override
  @Transactional
  public ApiResponse<Notification> createforTest(NotificationRequest rq) {
    return ApiResponse.<Notification>builder()
      .message("Message loaded")
      .code(200)
      .data(createNotification(rq.getUserId(), rq.getTitle(), rq.getType(), rq.getChannel()))
      .build();
  }

}
