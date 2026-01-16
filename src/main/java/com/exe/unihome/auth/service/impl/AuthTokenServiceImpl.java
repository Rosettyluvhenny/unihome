package com.exe.unihome.auth.service.impl;

import com.exe.unihome.auth.model.AuthResult;
import com.exe.unihome.auth.model.AuthenticationResponse;
import com.exe.unihome.auth.service.AuthTokenService;
import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.config.JwtProperties;
import com.exe.unihome.persistence.entity.identityAndAuth.RefreshToken;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import com.exe.unihome.persistence.repository.RefreshTokenRepository;
import com.exe.unihome.persistence.repository.UserRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
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
    try {
      // Create JWS Header
      JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

      // ...existing code...
      LocalDateTime now = LocalDateTime.now();
      LocalDateTime expiresAt = now.plus(getValidDuration(), ChronoUnit.SECONDS);

      // Create JWT Claims
      JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
        .subject(user.getEmail())
        .issuer("exe2.com")
        .issueTime(convertToDate(now))
        .expirationTime(convertToDate(expiresAt))
        .jwtID(UUID.randomUUID().toString())
        .claim("role", user.getRole())
        .claim("userId", user.getId())
        .build();

      // ...existing code...
      Payload payload = new Payload(jwtClaimsSet.toJSONObject());

      // Create JWS Object
      JWSObject jwsObject = new JWSObject(header, payload);
      // Sign the JWS object using HMAC-SHA512
      jwsObject.sign(new MACSigner((getSigningKey())));

      // Serialize to compact form
      return jwsObject.serialize();
    } catch (JOSEException ex) {
      throw new AppException(ErrorCode.TOKEN_PARSE_ERROR);
    }
  }

  private long getValidDuration() {
    return jwtProperties.getValidDuration();
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

  @Override
  public boolean verifyToken(String token) throws JOSEException, ParseException {
    JWSVerifier verifier = new MACVerifier(getSigningKey().getBytes());

    SignedJWT signedJWT = SignedJWT.parse(token);

    return signedJWT.verify(verifier);
  }

  private String getSigningKey() {
    return jwtProperties.getSignerKey();
  }

  /**
   * Convert LocalDateTime to Date for JWT operations
   */
  private Date convertToDate(LocalDateTime localDateTime) {
    return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
  }

  private record TokenParts(String id, String secret) {
  }

  private record RefreshTokenResult(String rawToken, RefreshToken refreshToken) {
  }
}
