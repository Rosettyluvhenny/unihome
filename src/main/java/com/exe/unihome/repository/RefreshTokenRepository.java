package com.exe.unihome.repository;

import com.exe.unihome.entity.identityAndAuth.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, String> {
  void deleteByRevokedAtIsNotNullOrExpiresAtBefore(LocalDateTime cutoff);
}
