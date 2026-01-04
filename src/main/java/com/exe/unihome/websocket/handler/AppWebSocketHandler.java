package com.exe.unihome.websocket.handler;

import com.exe.unihome.websocket.session.SessionRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
@Slf4j
@RequiredArgsConstructor
public class AppWebSocketHandler extends TextWebSocketHandler {

  private final SessionRegistry sessionRegistry;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    String userId = getUserId(session);
    sessionRegistry.register(userId, session);
    log.info("CONNECTED: {}| total={},userId= {}",
      session.getId(), sessionRegistry.size(), userId);
  }

  @Override
  protected void handleTextMessage(WebSocketSession session, TextMessage message) {
    log.info("MESSAGE: {}", message.getPayload());
    try {
      session.sendMessage(
        new TextMessage("echo: " + message.getPayload())
      );
    } catch (Exception e) {

    }
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session,
                                    CloseStatus status) {
    String userId = getUserId(session);
    sessionRegistry.remove(userId, session);
    log.info("DISCONNECTED: {} | reason={}  | total={} | userId= {}",
      session.getId(), status.getReason(), sessionRegistry.size(), userId);
  }

  private String getUserId(WebSocketSession session) {
    String query = session.getUri().getQuery();
    if (query == null) return "anonymous";

    for (String part : query.split("&")) {
      if (part.startsWith("userId=")) {
        return part.substring("userId=".length());
      }
    }
    return "anonymous";
  }
}
