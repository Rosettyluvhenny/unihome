package com.exe.unihome.auth.service.impl;

import com.exe.unihome.auth.service.VerifyTokenService;
import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.config.JwtProperties;
import com.exe.unihome.notification.service.NotificationService;
import com.exe.unihome.persistence.entity.identityAndAuth.Status;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import com.exe.unihome.persistence.entity.identityAndAuth.VerifyToken;
import com.exe.unihome.persistence.repository.UserRepository;
import com.exe.unihome.persistence.repository.VerifyTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerifyTokenServiceImpl implements VerifyTokenService {
  private final VerifyTokenRepository verifyTokenRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtProperties jwtProperties;

  private final NotificationService notificationService;

  @Override
  public String issueVerifyToken(String userId) {
    String tokenId = UUID.randomUUID().toString();
    String secret = UUID.randomUUID().toString();
    String rawToken = tokenId + "." + secret;

    LocalDateTime now = LocalDateTime.now();
    VerifyToken verifyToken = VerifyToken.builder()
      .id(tokenId)
      .userId(userId)
      .tokenHash(passwordEncoder.encode(secret))
      .issuedAt(now)
      .expiresAt(now.plus(Duration.ofHours(24))) // 24 hours validity
      .build();
    verifyTokenRepository.save(verifyToken);

    return rawToken;
  }

  @Override
  @Transactional
  public void verifyToken(String verifyTokenString) {
    TokenParts tokenParts = parseVerifyToken(verifyTokenString);
    VerifyToken verifyToken = getValidVerifyToken(tokenParts.id(), tokenParts.secret());

    // Mark token as revoked
    verifyToken.setRevokedAt(LocalDateTime.now());
    verifyTokenRepository.save(verifyToken);

    // Update user status to ACTIVE
    User user = userRepository.findById(verifyToken.getUserId())
      .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    user.setStatus(Status.ACTIVE);
    userRepository.save(user);
    notificationService.notifyVerifyEmailSuccess(user.getId());
  }

  @Override
  public VerifyToken getValidVerifyToken(String tokenId, String secret) {
    VerifyToken verifyToken = verifyTokenRepository.findById(tokenId)
      .orElseThrow(() -> new AppException(ErrorCode.VERIFY_TOKEN_INVALID));

    if (verifyToken.getRevokedAt() != null) {
      throw new AppException(ErrorCode.VERIFY_TOKEN_INVALID);
    }

    if (verifyToken.getExpiresAt().isBefore(LocalDateTime.now())) {
      throw new AppException(ErrorCode.VERIFY_TOKEN_EXPIRED);
    }

    if (!passwordEncoder.matches(secret, verifyToken.getTokenHash())) {
      throw new AppException(ErrorCode.VERIFY_TOKEN_INVALID);
    }

    return verifyToken;
  }

  private TokenParts parseVerifyToken(String verifyTokenString) {
    if (verifyTokenString == null || verifyTokenString.isBlank()) {
      throw new AppException(ErrorCode.VERIFY_TOKEN_INVALID);
    }

    String[] parts = verifyTokenString.split("\\.", 2);
    if (parts.length != 2) {
      throw new AppException(ErrorCode.VERIFY_TOKEN_INVALID);
    }
    return new TokenParts(parts[0], parts[1]);
  }

  private record TokenParts(String id, String secret) {
  }
}

