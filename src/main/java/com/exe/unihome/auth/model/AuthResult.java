package com.exe.unihome.auth.model;

public record AuthResult(AuthenticationResponse response, String refreshToken) {
}
