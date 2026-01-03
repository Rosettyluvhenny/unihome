package com.exe.unihome.notification.publisher;

import com.exe.unihome.notification.NotificationEvent;

public interface NotificationPublisher {
  void publish(NotificationEvent event);
}
