package com.exe.unihome.notification.controller;


import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.notification.NotificationRequest;
import com.exe.unihome.notification.service.NotificationService;
import com.exe.unihome.persistence.entity.notification.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationQueryController {

  private final NotificationService notificationService;

  @GetMapping("/unread")
  public ResponseEntity<ApiResponse<Page<Notification>>> getAllUnreadNotifications(Pageable pageable) {
    return ResponseEntity.ok(notificationService.getAllNotificationsByUserIdAndUnread(pageable));
  }

  @GetMapping("/latest-unread")
  public ResponseEntity<ApiResponse<Page<Notification>>> getLatestUnreadNotifications(Pageable pageable) {
    return ResponseEntity.ok(notificationService.getLatestNotificationsByUserIdAndUnread(pageable));
  }

  @PostMapping()
  public ResponseEntity<ApiResponse<Notification>> createTestNotification(@RequestBody NotificationRequest request) {
    return ResponseEntity.ok(notificationService.createforTest(request));
  }

}
