package com.exe.unihome.auth.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.auth.model.AuthenticationRequest;
import com.exe.unihome.auth.model.AuthResult;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import com.exe.unihome.persistence.repository.UserRepository;
import com.exe.unihome.auth.service.AuthTokenService;
import com.exe.unihome.auth.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthTokenService authTokenService;

  @Override
  public AuthResult authenticate(AuthenticationRequest request) {
    User user = userRepository.findByEmail(request.getEmail())
      .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new AppException(ErrorCode.INVALID_CREDENTIALS);
    }

    return authTokenService.issueTokens(user);
  }

  @Override
  public AuthResult refresh(String refreshTokenCookie) {
    return authTokenService.refreshTokens(refreshTokenCookie);
  }

  @Override
  public void logout(String refreshTokenCookie) {
    authTokenService.revoke(refreshTokenCookie);
  }
}
