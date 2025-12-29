package com.exe.unihome.service;

import com.exe.unihome.model.response.auth.AuthenticationResponse;

public record AuthResult(AuthenticationResponse response, String refreshToken) {
}
