package com.exe.unihome.service;

import com.exe.unihome.model.request.auth.AuthenticationRequest;

public interface AuthenticationService {
    AuthResult authenticate(AuthenticationRequest request);

    AuthResult refresh(String refreshTokenCookie);

    void logout(String refreshTokenCookie);
}
