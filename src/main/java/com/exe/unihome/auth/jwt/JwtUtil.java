package com.exe.unihome.auth.jwt;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class JwtUtil {
  private final JwtProperties jwtProperties;


  public String extractUserId(String token) {
    try {
      Claims claims = parseToken(token);
      return claims.getSubject();
    } catch (JwtException e) {
      throw new AppException(ErrorCode.INVALID_TOKEN);
    }
  }

  public String extractRole(String token) {
    try {
      Claims claims = parseToken(token);
      return (String) claims.get("role");
    } catch (JwtException e) {
      throw new AppException(ErrorCode.INVALID_TOKEN);
    }
  }

  public boolean validateToken(String token) {
    try {
      parseToken(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public Claims parseToken(String token) {
    return Jwts.parser()
      .verifyWith(getSigningKey())
      .build()
      .parseSignedClaims(token)
      .getPayload();
  }


  private boolean isTokenExpired(Claims claims) {
    return claims.getExpiration().before(new java.util.Date());
  }

  private SecretKey getSigningKey() {
    return Keys.hmacShaKeyFor(jwtProperties.getSignerKey().getBytes(StandardCharsets.UTF_8));
  }
}

