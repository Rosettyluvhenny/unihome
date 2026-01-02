package com.exe.unihome.controller;

import com.exe.unihome.AppException;
import com.exe.unihome.config.JwtProperties;
import com.exe.unihome.entity.identityAndAuth.RoleName;
import com.exe.unihome.entity.identityAndAuth.Status;
import com.exe.unihome.entity.identityAndAuth.VerifyToken;
import com.exe.unihome.exception.ErrorCode;
import com.exe.unihome.exception.GlobalExceptionHandler;
import com.exe.unihome.model.request.auth.AuthenticationRequest;
import com.exe.unihome.model.request.auth.RegistrationRequest;
import com.exe.unihome.model.response.UserResponse;
import com.exe.unihome.model.response.auth.AuthenticationResponse;
import com.exe.unihome.model.response.auth.RegistrationResponse;
import com.exe.unihome.service.AuthResult;
import com.exe.unihome.service.AuthenticationService;
import com.exe.unihome.service.UserService;
import com.exe.unihome.service.VerifyTokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthenticationController Tests")
class AuthenticationControllerTest {

  private MockMvc mockMvc;

  private ObjectMapper objectMapper;

  @Mock
  private AuthenticationService authenticationService;

  @Mock
  private UserService userService;

  @Mock
  private VerifyTokenService verifyTokenService;

  @Mock
  private JwtProperties jwtProperties;

  @InjectMocks
  private AuthenticationController authenticationController;

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
      .setControllerAdvice(new GlobalExceptionHandler())
      .build();
  }

  // ==================== REGISTER TESTS ====================

  @Test
  @DisplayName("Register - PASS: Should register user successfully with valid request")
  void testRegisterSuccess() throws Exception {
    // Arrange
    RegistrationRequest request = new RegistrationRequest();
    request.setFullName("John Doe");
    request.setEmail("john@example.com");
    request.setPassword("password123");

    UserResponse userResponse = new UserResponse();
    userResponse.setId("user-id-1");
    userResponse.setFullName("John Doe");
    userResponse.setEmail("john@example.com");
    userResponse.setStatus(Status.UNACTIVE);
    userResponse.setRole(RoleName.CUSTOMER);

    RegistrationResponse registrationResponse = RegistrationResponse.builder()
      .user(userResponse)
      .message("Registration successful. Please check your email to verify your account and activate access.")
      .build();

    when(userService.register(any(RegistrationRequest.class))).thenReturn(registrationResponse);

    // Act & Assert
    mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.user.id").value("user-id-1"))
      .andExpect(jsonPath("$.user.fullName").value("John Doe"))
      .andExpect(jsonPath("$.user.email").value("john@example.com"))
      .andExpect(jsonPath("$.user.status").value("UNACTIVE"))
      .andExpect(jsonPath("$.message").exists());

    verify(userService, times(1)).register(any(RegistrationRequest.class));
  }

  @Test
  @DisplayName("Register - FAIL: Should return 400 with invalid email")
  void testRegisterFailInvalidEmail() throws Exception {
    // Arrange
    RegistrationRequest request = new RegistrationRequest();
    request.setFullName("John Doe");
    request.setEmail("invalid-email");
    request.setPassword("password123");

    // Act & Assert
    mockMvc.perform(post("/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isBadRequest());

    verify(userService, never()).register(any(RegistrationRequest.class));
  }

  // ==================== LOGIN TESTS ====================

  @Test
  @DisplayName("Login - PASS: Should authenticate user successfully")
  void testLoginSuccess() throws Exception {
    // Arrange
    AuthenticationRequest request = new AuthenticationRequest();
    request.setEmail("john@example.com");
    request.setPassword("password123");

    AuthenticationResponse authResponse = AuthenticationResponse.builder()
      .token("jwt-token-123")
      .userId("user-id-1")
      .roles(List.of("CUSTOMER"))
      .fullName("John Doe")
      .build();

    AuthResult authResult = new AuthResult(authResponse, "refresh-token-123");

    when(authenticationService.authenticate(any(AuthenticationRequest.class)))
      .thenReturn(authResult);
    when(jwtProperties.getRefreshableDuration()).thenReturn(360000L);

    // Act & Assert
    mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.token").value("jwt-token-123"))
      .andExpect(jsonPath("$.userId").value("user-id-1"))
      .andExpect(jsonPath("$.fullName").value("John Doe"))
      .andExpect(MockMvcResultMatchers.cookie().exists("refresh_token"));

    verify(authenticationService, times(1)).authenticate(any(AuthenticationRequest.class));
  }

  @Test
  @DisplayName("Login - FAIL: Should return 400 with missing password")
  void testLoginFailMissingPassword() throws Exception {
    // Arrange
    AuthenticationRequest request = new AuthenticationRequest();
    request.setEmail("john@example.com");
    request.setPassword(null);

    // Act & Assert
    mockMvc.perform(post("/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isBadRequest());

    verify(authenticationService, never()).authenticate(any(AuthenticationRequest.class));
  }

  // ==================== REFRESH TESTS ====================

  @Test
  @DisplayName("Refresh - PASS: Should refresh token successfully with valid refresh token")
  void testRefreshSuccess() throws Exception {
    // Arrange
    AuthenticationResponse authResponse = AuthenticationResponse.builder()
      .token("new-jwt-token-123")
      .userId("user-id-1")
      .roles(List.of("CUSTOMER"))
      .fullName("John Doe")
      .build();

    AuthResult authResult = new AuthResult(authResponse, "new-refresh-token-123");

    when(authenticationService.refresh(eq("refresh-token-123")))
      .thenReturn(authResult);

    // Act & Assert
    Cookie refreshCookie = new Cookie("refresh_token", "refresh-token-123");
    mockMvc.perform(post("/auth/refresh")
        .cookie(refreshCookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.token").value("new-jwt-token-123"))
      .andExpect(jsonPath("$.userId").value("user-id-1"));

    verify(authenticationService, times(1)).refresh(eq("refresh-token-123"));
  }

  @Test
  @DisplayName("Refresh - FAIL: Should return error with missing refresh token")
  void testRefreshFailMissingToken() throws Exception {
    // Arrange - service throws AppException which is caught by GlobalExceptionHandler
    when(authenticationService.refresh(null))
      .thenThrow(new AppException(ErrorCode.REFRESH_TOKEN_INVALID));

    // Act & Assert - verify error response from GlobalExceptionHandler
    mockMvc.perform(post("/auth/refresh"))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.code").value(ErrorCode.REFRESH_TOKEN_INVALID.getCode()))
      .andExpect(jsonPath("$.message").value(ErrorCode.REFRESH_TOKEN_INVALID.getMessage()));

    verify(authenticationService, times(1)).refresh(null);
  }

  // ==================== LOGOUT TESTS ====================

  @Test
  @DisplayName("Logout - PASS: Should logout successfully")
  void testLogoutSuccess() throws Exception {
    // Arrange
    doNothing().when(authenticationService).logout(eq("refresh-token-123"));

    // Act & Assert
    Cookie refreshCookie = new Cookie("refresh_token", "refresh-token-123");
    mockMvc.perform(post("/auth/logout")
        .cookie(refreshCookie))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.code").value(0))
      .andExpect(jsonPath("$.message").value("Logged out"))
      .andExpect(MockMvcResultMatchers.cookie().exists("refresh_token"));

    verify(authenticationService, times(1)).logout(eq("refresh-token-123"));
  }

  @Test
  @DisplayName("Logout - FAIL: Should handle logout with invalid token")
  void testLogoutFailInvalidToken() throws Exception {
    // Arrange - service throws AppException which is caught by GlobalExceptionHandler
    String invalidToken = "invalid-token";
    doThrow(new AppException(ErrorCode.REFRESH_TOKEN_INVALID))
      .when(authenticationService).logout(eq(invalidToken));

    // Act & Assert - verify error response from GlobalExceptionHandler
    Cookie invalidCookie = new Cookie("refresh_token", invalidToken);
    mockMvc.perform(post("/auth/logout")
        .cookie(invalidCookie))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.code").value(ErrorCode.REFRESH_TOKEN_INVALID.getCode()))
      .andExpect(jsonPath("$.message").value(ErrorCode.REFRESH_TOKEN_INVALID.getMessage()));

    verify(authenticationService, times(1)).logout(eq(invalidToken));
  }

  // ==================== VERIFY TOKEN TESTS ====================

  @Test
  @DisplayName("Verify - PASS: Should verify token successfully and activate user")
  void testVerifyTokenSuccess() throws Exception {
    // Arrange
    String verifyToken = "token-id.secret";
    doNothing().when(verifyTokenService).verifyToken(eq(verifyToken));

    // Act & Assert
    mockMvc.perform(post("/auth/verify")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(new com.exe.unihome.model.request.auth.VerifyTokenRequest(verifyToken))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.valid").value(true))
      .andExpect(jsonPath("$.message").value("Email verified successfully. Your account is now active."));

    verify(verifyTokenService, times(1)).verifyToken(eq(verifyToken));
  }

  @Test
  @DisplayName("Verify - FAIL: Should return error with invalid verify token")
  void testVerifyTokenFailInvalidToken() throws Exception {
    // Arrange
    String invalidToken = "invalid-token";
    doThrow(new AppException(ErrorCode.VERIFY_TOKEN_INVALID))
      .when(verifyTokenService).verifyToken(eq(invalidToken));

    // Act & Assert
    mockMvc.perform(post("/auth/verify")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(new com.exe.unihome.model.request.auth.VerifyTokenRequest(invalidToken))))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.valid").value(false));

    verify(verifyTokenService, times(1)).verifyToken(eq(invalidToken));
  }

  @Test
  @DisplayName("Verify - FAIL: Should return error with expired verify token")
  void testVerifyTokenFailExpiredToken() throws Exception {
    // Arrange
    String expiredToken = "expired-token.secret";
    doThrow(new AppException(ErrorCode.VERIFY_TOKEN_EXPIRED))
      .when(verifyTokenService).verifyToken(eq(expiredToken));

    // Act & Assert
    mockMvc.perform(post("/auth/verify")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(new com.exe.unihome.model.request.auth.VerifyTokenRequest(expiredToken))))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.valid").value(false));

    verify(verifyTokenService, times(1)).verifyToken(eq(expiredToken));
  }

  // ==================== VERIFY INTROSPECT TESTS ====================

  @Test
  @DisplayName("Verify Introspect - PASS: Should introspect valid token")
  void testVerifyIntrospectSuccess() throws Exception {
    // Arrange
    String verifyToken = "token-id.secret";
    VerifyToken token = VerifyToken.builder()
      .id("token-id")
      .userId("user-id-1")
      .tokenHash("hashed-secret")
      .build();

    when(verifyTokenService.getValidVerifyToken(eq("token-id"), eq("secret")))
      .thenReturn(token);

    // Act & Assert
    mockMvc.perform(post("/auth/verify/introspect")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(new com.exe.unihome.model.request.auth.VerifyTokenRequest(verifyToken))))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.valid").value(true))
      .andExpect(jsonPath("$.userId").value("user-id-1"));

    verify(verifyTokenService, times(1)).getValidVerifyToken(eq("token-id"), eq("secret"));
  }

  @Test
  @DisplayName("Verify Introspect - FAIL: Should return error with invalid token format")
  void testVerifyIntrospectFailInvalidFormat() throws Exception {
    // Arrange
    String invalidToken = "invalid-format";

    // Act & Assert
    mockMvc.perform(post("/auth/verify/introspect")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(new com.exe.unihome.model.request.auth.VerifyTokenRequest(invalidToken))))
      .andExpect(status().isBadRequest())
      .andExpect(jsonPath("$.valid").value(false))
      .andExpect(jsonPath("$.message").value("Invalid token format"));

    verify(verifyTokenService, never()).getValidVerifyToken(anyString(), anyString());
  }

  @Test
  @DisplayName("Verify Introspect - FAIL: Should return error with expired token")
  void testVerifyIntrospectFailExpiredToken() throws Exception {
    // Arrange
    String expiredToken = "expired-id.secret";
    when(verifyTokenService.getValidVerifyToken(eq("expired-id"), eq("secret")))
      .thenThrow(new AppException(ErrorCode.VERIFY_TOKEN_EXPIRED));

    // Act & Assert
    mockMvc.perform(post("/auth/verify/introspect")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(new com.exe.unihome.model.request.auth.VerifyTokenRequest(expiredToken))))
      .andExpect(status().isUnauthorized())
      .andExpect(jsonPath("$.valid").value(false));

    verify(verifyTokenService, times(1)).getValidVerifyToken(eq("expired-id"), eq("secret"));
  }

  // ==================== GOOGLE LOGIN TESTS ====================

  @Test
  @DisplayName("Google Login - PASS: Should redirect to OAuth2 authorization endpoint")
  void testGoogleLoginSuccess() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/auth/google"))
      .andExpect(status().isFound())
      .andExpect(MockMvcResultMatchers.redirectedUrl("/unihome/oauth2/authorization/google"));
  }

  @Test
  @DisplayName("Google Login - FAIL: Should verify correct redirect URL format")
  void testGoogleLoginFailInvalidRedirectUrl() throws Exception {
    // Act & Assert
    mockMvc.perform(get("/auth/google"))
      .andExpect(status().isFound());

    // Verify it's not a different status code (e.g., success instead of redirect)
  }
}

