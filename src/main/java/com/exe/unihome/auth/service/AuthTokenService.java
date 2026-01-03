package com.exe.unihome.auth.service;

import com.exe.unihome.auth.model.AuthResult;
import com.exe.unihome.persistence.entity.identityAndAuth.User;

public interface AuthTokenService {
  AuthResult issueTokens(User user);

  AuthResult refreshTokens(String refreshTokenCookie);

  void revoke(String refreshTokenCookie);
}
