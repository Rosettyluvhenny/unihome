package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.identityAndAuth.VerifyToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerifyTokenRepository extends JpaRepository<VerifyToken, String> {
  Optional<VerifyToken> findByUserIdAndRevokedAtIsNull(String userId);

  void deleteByRevokedAtIsNotNullOrExpiresAtBefore(LocalDateTime cutoff);
}

