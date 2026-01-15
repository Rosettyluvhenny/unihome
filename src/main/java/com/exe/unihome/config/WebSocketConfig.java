package com.exe.unihome.config;

import com.exe.unihome.websocket.JwtHandshakeInterceptor;
import com.exe.unihome.websocket.handler.UnifiedWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@Profile("!test")
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

  private final UnifiedWebSocketHandler unifiedWebSocketHandler;
  private final JwtHandshakeInterceptor interceptor;

  @Override
  public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    registry.addHandler(unifiedWebSocketHandler, "/ws")
      .addInterceptors(interceptor)
      .setAllowedOrigins("*");
  }

}
