package com.exe.unihome.auth.service;

import com.exe.unihome.auth.model.AuthenticationRequest;
import com.exe.unihome.auth.model.AuthResult;

public interface AuthenticationService {
  AuthResult authenticate(AuthenticationRequest request);

  AuthResult refresh(String refreshTokenCookie);

  void logout(String refreshTokenCookie);
}
