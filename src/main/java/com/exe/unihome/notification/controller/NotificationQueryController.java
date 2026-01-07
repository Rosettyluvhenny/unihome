package com.exe.unihome.notification.controller;


import com.exe.unihome.persistence.entity.notification.Notification;
import com.exe.unihome.persistence.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
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
  public List<Notification> unread(@RequestParam String userId) {
    return repository.findByUserIdOrderByCreatedAtDesc(userId);
  }
}
