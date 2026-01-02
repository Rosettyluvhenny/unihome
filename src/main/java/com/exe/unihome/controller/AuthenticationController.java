package com.exe.unihome.controller;

import com.exe.unihome.config.JwtProperties;
import com.exe.unihome.model.request.auth.AuthenticationRequest;
import com.exe.unihome.model.request.auth.RegistrationRequest;
import com.exe.unihome.model.request.auth.VerifyTokenRequest;
import com.exe.unihome.model.response.ApiResponse;
import com.exe.unihome.model.response.auth.RegistrationResponse;
import com.exe.unihome.model.response.auth.AuthenticationResponse;
import com.exe.unihome.model.response.auth.VerifyTokenResponse;
import com.exe.unihome.service.AuthResult;
import com.exe.unihome.service.AuthenticationService;
import com.exe.unihome.service.UserService;
import com.exe.unihome.service.VerifyTokenService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final UserService userService;
    private final VerifyTokenService verifyTokenService;
    private final JwtProperties jwtProperties;

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody RegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody AuthenticationRequest request) {
        AuthResult result = authenticationService.authenticate(request);
        ResponseCookie cookie = buildRefreshCookie(result.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.response());
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(
            @CookieValue(value = "refresh_token", required = false) String refreshToken) {
        AuthResult result = authenticationService.refresh(refreshToken);
        ResponseCookie cookie = buildRefreshCookie(result.refreshToken());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(result.response());
    }

    @GetMapping("/verify")
    public ResponseEntity<VerifyTokenResponse> verifyToken(@NotBlank @RequestParam String token) {
        try {
            verifyTokenService.verifyToken(token);
            return ResponseEntity.ok(VerifyTokenResponse.builder()
                    .valid(true)
                    .message("Email verified successfully. Your account is now active.")
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(VerifyTokenResponse.builder()
                            .valid(false)
                            .message("Verification failed: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(value = "refresh_token", required = false) String refreshToken) {
        authenticationService.logout(refreshToken);
        ResponseCookie cookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/unihome/auth/refresh")
                .maxAge(Duration.ZERO)
                .build();
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(ApiResponse.<Void>builder()
                        .code(0)
                        .message("Logged out")
                        .build());
    }

    @GetMapping("/google")
    public ResponseEntity<Void> googleLogin() {
        return ResponseEntity.status(HttpStatus.FOUND)
                .header(HttpHeaders.LOCATION, "/unihome/oauth2/authorization/google")
                .build();
    }

    private ResponseCookie buildRefreshCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/unihome/auth/refresh")
                .maxAge(Duration.ofSeconds(jwtProperties.getRefreshableDuration()))
                .build();
    }
}
