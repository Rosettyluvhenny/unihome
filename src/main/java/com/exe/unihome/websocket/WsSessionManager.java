package com.exe.unihome.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WsSessionManager {

  private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

  public void addSession(String userId, WebSocketSession session) {
    sessions.put(userId, session);
  }

  public void removeSession(String userId) {
    sessions.remove(userId);
  }

  public WebSocketSession getSession(String userId) {
    return sessions.get(userId);
  }

  public boolean isUserOnline(String userId) {
    WebSocketSession session = sessions.get(userId);
    return session != null && session.isOpen();
  }

  public Map<String, WebSocketSession> getAllSessions() {
    return new ConcurrentHashMap<>(sessions);
  }
}
