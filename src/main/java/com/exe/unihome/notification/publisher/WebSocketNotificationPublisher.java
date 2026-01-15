package com.exe.unihome.notification.publisher;

import com.exe.unihome.persistence.entity.notification.Notification;
import com.exe.unihome.websocket.dto.WsMessage;
import com.exe.unihome.websocket.enums.WsMessageAction;
import com.exe.unihome.websocket.enums.WsMessageType;
import com.exe.unihome.websocket.session.WsSessionManager;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * Publishes notifications via WebSocket using the unified WsMessage envelope.
 * All notifications are wrapped in the standard envelope format:
 * {
 * "type": "NOTIFICATION",
 * "action": "NEW_NOTIFICATION",
 * "payload": { notification details }
 * }
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketNotificationPublisher {

  private final WsSessionManager sessionRegistry;
  private final ObjectMapper objectMapper;

  public void push(Notification notification) {
    for (WebSocketSession session :
      sessionRegistry.getSessions(notification.getUserId())) {

      try {
        // Wrap notification in unified WsMessage envelope
        WsMessage wsMessage = WsMessage.builder()
          .type(WsMessageType.NOTIFICATION)
          .action(WsMessageAction.NEW_NOTIFICATION)
          .payload(notification)
          .build();

        String json = objectMapper.writeValueAsString(wsMessage);
        synchronized (session) {
          session.sendMessage(new TextMessage(json));
        }
        log.debug(
          "Notification sent to user {} via session {}",
          notification.getUserId(),
          session.getId()
        );

      } catch (Exception e) {
        // 3️⃣ Gửi fail → remove session
        sessionRegistry.removeSession(notification.getUserId(), session);

        log.warn(
          "Failed to send notification to user {} via session {}",
          notification.getUserId(),
          session.getId(),
          e
        );
      }
    }
  }
}
