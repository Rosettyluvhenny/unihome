package com.exe.unihome.notification.publisher;

import com.exe.unihome.persistence.entity.notification.Notification;
import com.exe.unihome.websocket.dto.WsMessage;
import com.exe.unihome.websocket.enums.WsMessageAction;
import com.exe.unihome.websocket.enums.WsMessageType;
import com.exe.unihome.websocket.session.SessionRegistry;
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

  private final SessionRegistry sessionRegistry;
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
        session.sendMessage(new TextMessage(json));
        log.debug("Notification sent to user {} via WebSocket", notification.getUserId());
      } catch (Exception e) {
        log.warn("Failed to send notification to user {} via WebSocket", notification.getUserId(), e);
        // ignore – reconnect handled elsewhere
      }
    }
  }
}
