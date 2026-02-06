package com.exe.unihome.config;

import com.exe.unihome.auth.model.AuthResult;
import com.exe.unihome.auth.service.AuthTokenService;
import com.exe.unihome.auth.service.UserService;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
@Profile({"!test"})
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {
  private final UserService userService;
  private final AuthTokenService authTokenService;
  private final JwtProperties jwtProperties;
  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                      Authentication authentication) throws IOException, ServletException {
    if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      return;
    }

    OAuth2User oauthUser = oauthToken.getPrincipal();
    String email = getAttribute(oauthUser, "email");
    String fullName = getAttribute(oauthUser, "name");

    if (email == null || email.isBlank()) {
      response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
        .code(ErrorCode.INVALID_REQUEST.getCode())
        .message("Google account email is required")
        .build();
      objectMapper.writeValue(response.getWriter(), apiResponse);
      return;
    }

    User user = userService.getOrCreateGoogleUser(email, fullName);
    AuthResult result = authTokenService.issueTokens(user);

    ResponseCookie cookie = ResponseCookie.from("refresh_token", result.refreshToken())
      .httpOnly(true)
      .secure(true)
      .sameSite("Strict")
      .path("/unihome/auth/refresh")
      .maxAge(Duration.ofSeconds(jwtProperties.getRefreshableDuration()))
      .build();

//    response.setStatus(HttpServletResponse.SC_OK);
//    response.setHeader(HttpHeaders.SET_COOKIE, cookie.toString());
//    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
//    objectMapper.writeValue(response.getWriter(), result.response());
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

// gửi access token qua fragment (không lộ lên server log)
    String redirectUrl =
      "https://unihome-smoky.vercel.app/oauth2/success#accessToken=" + result.response().getToken();

    response.sendRedirect(redirectUrl);
  }

  private String getAttribute(OAuth2User user, String key) {
    Object value = user.getAttributes().get(key);
    return value != null ? value.toString() : null;
  }
}
