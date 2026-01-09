package com.exe.unihome.scheduler;

import com.exe.unihome.persistence.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupJob {
  private final RefreshTokenRepository repository;

  @Scheduled(cron = "0 0 3 * * *")
  public void cleanup() {
    repository.deleteByRevokedAtIsNotNullOrExpiresAtBefore(LocalDateTime.now());
  }
}
