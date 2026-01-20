package com.exe.unihome.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {
  String userId;
  String title;
  NotificationType type;
  NotificationChannel channel;
}
