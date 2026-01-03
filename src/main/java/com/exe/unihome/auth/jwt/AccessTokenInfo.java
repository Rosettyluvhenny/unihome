package com.exe.unihome.auth.jwt;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class AccessTokenInfo {
  private String userId;
  private String role;
  private Instant issuedAt;
  private Instant expiresAt;
  private boolean isActive;
}

