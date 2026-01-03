package com.exe.unihome.auth.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.config.JwtProperties;
import com.exe.unihome.auth.model.AuthResult;
import com.exe.unihome.auth.model.AuthenticationResponse;
import com.exe.unihome.persistence.entity.identityAndAuth.RefreshToken;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import com.exe.unihome.persistence.repository.RefreshTokenRepository;
import com.exe.unihome.persistence.repository.UserRepository;
import com.exe.unihome.auth.service.AuthTokenService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthTokenServiceImpl implements AuthTokenService {
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProperties jwtProperties;

  @Override
  public AuthResult issueTokens(User user) {
    String accessToken = generateAccessToken(user);
    RefreshTokenResult refreshTokenResult = issueRefreshToken(user.getId());
    AuthenticationResponse response = AuthenticationResponse.builder()
      .token(accessToken)
      .userId(user.getId())
      .fullName(user.getFullName())
      .roles(List.of(user.getRole().name()))
      .build();
    return new AuthResult(response, refreshTokenResult.rawToken());
  }

  @Override
  public AuthResult refreshTokens(String refreshTokenCookie) {
    TokenParts tokenParts = parseRefreshToken(refreshTokenCookie);
    RefreshToken refreshToken = refreshTokenRepository.findById(tokenParts.id())
      .orElseThrow(() -> new AppException(ErrorCode.REFRESH_TOKEN_INVALID));

    validateRefreshToken(refreshToken, tokenParts.secret());
    refreshToken.setRevokedAt(LocalDateTime.now());
    refreshTokenRepository.save(refreshToken);

    User user = userRepository.findById(refreshToken.getUserId())
      .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    return issueTokens(user);
  }

  @Override
  public void revoke(String refreshTokenCookie) {
    if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
      return;
    }

    TokenParts tokenParts = parseRefreshToken(refreshTokenCookie);
    refreshTokenRepository.findById(tokenParts.id())
      .ifPresent(refreshToken -> {
        if (refreshToken.getRevokedAt() == null
          && passwordEncoder.matches(tokenParts.secret(), refreshToken.getTokenHash())) {
          refreshToken.setRevokedAt(LocalDateTime.now());
          refreshTokenRepository.save(refreshToken);
        }
      });
  }

  private String generateAccessToken(User user) {
    LocalDateTime now = LocalDateTime.now();
    Date issuedAt = Date.from(now.atZone(java.time.ZoneId.systemDefault()).toInstant());
    Date expiryDate = Date.from(now.plusSeconds(jwtProperties.getValidDuration())
      .atZone(java.time.ZoneId.systemDefault()).toInstant());

    return Jwts.builder()
      .setSubject(user.getId())
      .claim("role", user.getRole().name())
      .setIssuedAt(issuedAt)
      .setExpiration(expiryDate)
      .signWith(getSigningKey(), SignatureAlgorithm.HS512)
      .compact();
  }

  private RefreshTokenResult issueRefreshToken(String userId) {
    String tokenId = UUID.randomUUID().toString();
    String secret = UUID.randomUUID().toString();
    String rawToken = tokenId + "." + secret;

    LocalDateTime now = LocalDateTime.now();
    RefreshToken refreshToken = RefreshToken.builder()
      .id(tokenId)
      .userId(userId)
      .tokenHash(passwordEncoder.encode(secret))
      .issuedAt(now)
      .expiresAt(now.plus(Duration.ofSeconds(jwtProperties.getRefreshableDuration())))
      .build();
    refreshTokenRepository.save(refreshToken);

    return new RefreshTokenResult(rawToken, refreshToken);
  }

  private void validateRefreshToken(RefreshToken refreshToken, String secret) {
    if (refreshToken.getRevokedAt() != null) {
      throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
    }
    if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new AppException(ErrorCode.REFRESH_TOKEN_EXPIRED);
    }
    if (!passwordEncoder.matches(secret, refreshToken.getTokenHash())) {
      throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
    }
  }

  private TokenParts parseRefreshToken(String refreshTokenCookie) {
    if (refreshTokenCookie == null || refreshTokenCookie.isBlank()) {
      throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
    }

    String[] parts = refreshTokenCookie.split("\\.", 2);
    if (parts.length != 2) {
      throw new AppException(ErrorCode.REFRESH_TOKEN_INVALID);
    }
    return new TokenParts(parts[0], parts[1]);
  }

  private Key getSigningKey() {
    return Keys.hmacShaKeyFor(jwtProperties.getSignerKey().getBytes(StandardCharsets.UTF_8));
  }

  private record TokenParts(String id, String secret) {
  }

  private record RefreshTokenResult(String rawToken, RefreshToken refreshToken) {
  }
}
