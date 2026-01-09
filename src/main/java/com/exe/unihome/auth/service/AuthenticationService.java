package com.exe.unihome.auth.service;

import com.exe.unihome.auth.model.AuthResult;
import com.exe.unihome.auth.model.AuthenticationRequest;

public interface AuthenticationService {
  AuthResult authenticate(AuthenticationRequest request);

  AuthResult refresh(String refreshTokenCookie);

  void logout(String refreshTokenCookie);

  boolean verifyToken(String token);


}
