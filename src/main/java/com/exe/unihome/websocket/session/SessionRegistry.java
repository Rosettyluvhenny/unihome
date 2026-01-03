package com.exe.unihome.websocket.session;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

  private final Map<String, Set<WebSocketSession>> sessions = new ConcurrentHashMap<>();

  public void register(String userId, WebSocketSession session) {
    sessions
      .computeIfAbsent(userId, k -> ConcurrentHashMap.newKeySet())
      .add(session);
  }

  public void remove(String userId, WebSocketSession session) {
    Set<WebSocketSession> set = sessions.get(userId);
    if (set != null) {
      set.remove(session);
      if (set.isEmpty()) {
        sessions.remove(userId);
      }
    }
  }

  public Set<WebSocketSession> getSessions(String userId) {
    return sessions.getOrDefault(userId, Set.of());
  }

  public int size() {
    return sessions.values().stream().mapToInt(Set::size).sum();
  }
}
