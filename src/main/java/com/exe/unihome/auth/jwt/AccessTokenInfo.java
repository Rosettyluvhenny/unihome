package com.exe.unihome.auth.jwt;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class AccessTokenInfo {
  private String userId;
  private String role;
  private LocalDateTime issuedAt;
  private LocalDateTime expiresAt;
  private boolean isActive;
}

