package com.exe.unihome.websocket;

import com.exe.unihome.auth.service.AuthTokenService;
import com.exe.unihome.auth.service.UserService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.ParseException;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

  private final AuthTokenService authTokenService;
  private final UserService userService;

  @Override
  public boolean beforeHandshake(
    ServerHttpRequest request,
    ServerHttpResponse response,
    WebSocketHandler wsHandler,
    Map<String, Object> attributes) {

    String token = UriComponentsBuilder
      .fromUri(request.getURI())
      .build()
      .getQueryParams()
      .getFirst("token");

    if (token == null || token.isBlank()) {
      return false;
    }

    try {
      // Verify token validity
      if (!authTokenService.verifyToken(token)) {
        return false;
      }

      // Extract userId from token claims
      SignedJWT signedJWT = (SignedJWT) JWTParser.parse(token);
      String userId = signedJWT.getJWTClaimsSet().getStringClaim("userId");

      if (userId == null || userId.isBlank() || !userService.existById(userId)) {
        return false;
      }

      attributes.put("userId", userId);
      return true;
    } catch (JOSEException | ParseException e) {
      return false;
    }
  }

  @Override
  public void afterHandshake(
    ServerHttpRequest request,
    ServerHttpResponse response,
    WebSocketHandler wsHandler,
    Exception exception) {
  }
}
