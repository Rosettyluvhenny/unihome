package com.exe.unihome.auth.jwt;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IntrospectTokenResponse {
  private boolean valid;
}

