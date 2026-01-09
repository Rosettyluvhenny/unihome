package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.identityAndAuth.VerifyToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VerifyTokenRepository extends JpaRepository<VerifyToken, String> {
  Optional<VerifyToken> findByUserIdAndRevokedAtIsNullAndExpiresAtAfter(String userId, LocalDateTime expiresAtAfter);

  void deleteByRevokedAtIsNotNullOrExpiresAtBefore(LocalDateTime cutoff);

}

