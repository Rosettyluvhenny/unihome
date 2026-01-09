package com.exe.unihome.notification.controller;


import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.persistence.entity.notification.Notification;
import com.exe.unihome.persistence.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationQueryController {

  private final NotificationRepository repository;

  @GetMapping("/unread")
  public ResponseEntity<ApiResponse<List<Notification>>> unread(@RequestParam String userId) {
    return ResponseEntity.ok(ApiResponse.<List<Notification>>builder()
      .code(200)
      .message("Unread notifications retrieved successfully")
      .data(repository.findByUserIdOrderByCreatedAtDesc(userId))
      .build());
  }
}
