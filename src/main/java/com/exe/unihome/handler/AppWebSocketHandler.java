package com.exe.unihome.handler;

import com.exe.unihome.session.SessionRegistry;
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
    sessionRegistry.register(session);
    log.info("CONNECTED: {}| total={}",
      session.getId(), sessionRegistry.size());
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
    sessionRegistry.remove(session.getId());
    log.info("DISCONNECTED: {} | reason={}  | total={}",
      session.getId(), status.getReason(), sessionRegistry.size());
  }
}
