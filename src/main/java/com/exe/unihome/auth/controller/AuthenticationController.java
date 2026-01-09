package com.exe.unihome.auth.controller;

import com.exe.unihome.auth.jwt.IntrospectTokenResponse;
import com.exe.unihome.auth.jwt.VerifyTokenResponse;
import com.exe.unihome.auth.model.*;
import com.exe.unihome.auth.service.AuthenticationService;
import com.exe.unihome.auth.service.UserService;
import com.exe.unihome.auth.service.VerifyTokenService;
import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.config.JwtProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
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
  public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegistrationRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.<UserResponse>builder()
        .code(200)
        .message("Registration successful. Please check your email to verify your account and activate access.")
        .data(userService.register(request))
        .build());
  }

  @PostMapping("/login")
  public ResponseEntity<ApiResponse<AuthenticationResponse>> login(@Valid @RequestBody AuthenticationRequest request) {
    AuthResult result = authenticationService.authenticate(request);
    ResponseCookie cookie = buildRefreshCookie(result.refreshToken());
    return ResponseEntity.ok()
      .header(HttpHeaders.SET_COOKIE, cookie.toString())
      .body(ApiResponse.<AuthenticationResponse>builder()
        .code(200)
        .message("Login successful")
        .data(result.response())
        .build());
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<AuthenticationResponse>> refresh(
    @CookieValue(value = "refresh_token", required = false) String refreshToken) {
    AuthResult result = authenticationService.refresh(refreshToken);
    ResponseCookie cookie = buildRefreshCookie(result.refreshToken());
    return ResponseEntity.ok()
      .header(HttpHeaders.SET_COOKIE, cookie.toString())
      .body(ApiResponse.<AuthenticationResponse>builder()
        .code(200)
        .message("Token refreshed successfully")
        .data(result.response())
        .build());
  }

  @GetMapping("/verify")
  public ResponseEntity<ApiResponse<VerifyTokenResponse>> verifyToken(@NotBlank @RequestParam String token) {
    try {
      verifyTokenService.verifyToken(token);
      return ResponseEntity.ok(ApiResponse.<VerifyTokenResponse>builder()
        .code(200)
        .message("Email verified successfully")
        .data(VerifyTokenResponse.builder()
          .valid(true)
          .message("Your account is now active.")
          .build())
        .build());
    } catch (Exception e) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.<VerifyTokenResponse>builder()
          .code(200)
          .message("Verification failed: " + e.getMessage())
          .data(VerifyTokenResponse.builder()
            .valid(false)
            .message("Verification failed")
            .build())
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
        .code(200)
        .message("Logged out")
        .build());
  }

  @GetMapping("/google")
  public ResponseEntity<Void> googleLogin() {
    return ResponseEntity.status(HttpStatus.FOUND)
      .header(HttpHeaders.LOCATION, "/unihome/oauth2/authorization/google")
      .build();
  }

  @PostMapping("/introspect")
  public ResponseEntity<ApiResponse<IntrospectTokenResponse>> introspectToken(
    @RequestBody IntrospectRequest request) {

    boolean check = authenticationService.verifyToken(request.getToken());

    return ResponseEntity.ok(ApiResponse.<IntrospectTokenResponse>builder()
      .code(200)
      .message("Token introspection successful")
      .data(IntrospectTokenResponse.builder()
        .valid(check)
        .build())
      .build());
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

  @PostMapping("/resend")
  public ResponseEntity<ApiResponse<UserResponse>> resendToken(
    @RequestParam String email) {
    return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
      .code(200)
      .data(userService.resendVerification(email))
      .message("Please check your email to verify your account and activate access.")
      .build());
  }
}
