package com.exe.unihome.websocket.session;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

  private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

  public void register(WebSocketSession session) {
    sessions.put(session.getId(), session);
  }

  public void remove(String sessionId) {
    sessions.remove(sessionId);
  }

  public Collection<WebSocketSession> all() {
    return sessions.values();
  }

  public int size() {
    return sessions.size();
  }
}
