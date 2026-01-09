package com.exe.unihome.websocket.handler;

import com.exe.unihome.auth.jwt.JwtUtil;
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

  private final JwtUtil jwtUtil;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) {
    String token = getToken(session);
    if (jwtUtil.validateToken(token)) {
      String userId = jwtUtil.extractUserId(token);
      sessionRegistry.register(userId, session);
      log.info("CONNECTED: {}| total={},userId= {}",
        session.getId(), sessionRegistry.size(), token);
    }

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
    String userId = (String) session.getAttributes().get("userId");
    sessionRegistry.remove(userId, session);
    log.info("DISCONNECTED: {} | reason={}  | total={} | userId= {}",
      session.getId(), status.getReason(), sessionRegistry.size(), userId);
  }

  private String getToken(WebSocketSession session) {
    String query = session.getUri().getQuery();
    if (query == null) return "anonymous";

    for (String part : query.split("&")) {
      if (part.startsWith("token=")) {
        return part.substring("token=".length());
      }
    }
    return "anonymous";
  }
}
