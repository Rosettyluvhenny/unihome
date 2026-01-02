package com.exe.unihome.service;

import com.exe.unihome.entity.identityAndAuth.User;

public interface AuthTokenService {
  AuthResult issueTokens(User user);

  AuthResult refreshTokens(String refreshTokenCookie);

  void revoke(String refreshTokenCookie);
}
