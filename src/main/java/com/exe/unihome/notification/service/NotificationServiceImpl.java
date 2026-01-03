package com.exe.unihome.notification.service;

import com.exe.unihome.notification.NotificationChannel;
import com.exe.unihome.notification.publisher.WebSocketNotificationPublisher;
import com.exe.unihome.persistence.entity.notification.Notification;
import com.exe.unihome.persistence.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

  private final NotificationRepository repository;
  private final WebSocketNotificationPublisher wsPublisher;
  private final ObjectMapper objectMapper;

  public void notifyVerifyEmailSuccess(String userId) {
    Notification n = new Notification();
    n.setId(UUID.randomUUID().toString());
    n.setUserId(userId);
    n.setChannel(NotificationChannel.EMAIL.toString());
    n.setType("VERIFY_EMAIL_SUCCESS");
    n.setTitle("Xác nhận email thành công");
    n.setPayload(createPayload());
    n.setRead(false);

    repository.save(n);

    // push realtime nếu user online
    wsPublisher.push(n);
  }

  private ObjectNode createPayload() {
    ObjectNode root = objectMapper.createObjectNode();
    root.put("action", "OPEN_PROFILE");
    return root;
  }
}
