package com.exe.unihome.notification.publisher;

import com.exe.unihome.persistence.entity.notification.Notification;
import com.exe.unihome.websocket.session.SessionRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
@RequiredArgsConstructor
public class WebSocketNotificationPublisher {

  private final SessionRegistry sessionRegistry;
  private final ObjectMapper objectMapper;

  public void push(Notification notification) {
    for (WebSocketSession session :
      sessionRegistry.getSessions(notification.getUserId())) {

      try {
        String json = objectMapper.writeValueAsString(notification);
        session.sendMessage(new TextMessage(json));
      } catch (Exception e) {
        // ignore – reconnect handled elsewhere
      }
    }
  }
}
