package com.exe.unihome.service.impl;

import com.exe.unihome.AppException;
import com.exe.unihome.entity.User;
import com.exe.unihome.exception.ErrorCode;
import com.exe.unihome.model.request.auth.AuthenticationRequest;
import com.exe.unihome.repository.UserRepository;
import com.exe.unihome.service.AuthResult;
import com.exe.unihome.service.AuthTokenService;
import com.exe.unihome.service.AuthenticationService;
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
