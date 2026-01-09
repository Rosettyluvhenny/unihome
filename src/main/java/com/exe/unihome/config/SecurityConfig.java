package com.exe.unihome.config;

import com.exe.unihome.auth.jwt.JwtAuthenticationFilter;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.common.model.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@EnableMethodSecurity
public class SecurityConfig {
  private final ObjectProvider<OAuth2SuccessHandler> oAuth2SuccessHandlerProvider;
  private final JwtAuthenticationFilter jwtAuthenticationFilter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @Bean
  public OAuth2UserService<?, OAuth2User> oauth2UserService() {
    return new DefaultOAuth2UserService();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
      .csrf(csrf -> csrf.disable())
      .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
      .authorizeHttpRequests(auth -> auth
        .requestMatchers(
          "/auth/**",
          "/oauth2/**",
          "/login/oauth2/**",
          "/mail/**",
          "/actuator/**",
          "/v3/api-docs/**",
          "/redis-test/**",
          "/swagger-ui/**",
          "/users/**",
          "/swagger-ui.html",
          "/ws/**")
        .permitAll()
        .anyRequest().authenticated())
      .exceptionHandling(exception -> exception
        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
        .accessDeniedHandler(accessDeniedHandler()))
      .oauth2Login(oauth2 -> {
        oauth2
          .userInfoEndpoint(userInfo ->
            userInfo.userService((OAuth2UserService<OAuth2UserRequest, OAuth2User>) oauth2UserService()));
        OAuth2SuccessHandler oAuth2SuccessHandler = oAuth2SuccessHandlerProvider.getIfAvailable();
        if (oAuth2SuccessHandler != null)
          oauth2.successHandler(oAuth2SuccessHandler);
      })

      .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
      .httpBasic(Customizer.withDefaults());
    return http.build();
  }

  @Bean
  public AccessDeniedHandler accessDeniedHandler() {
    return ((request, response, accessDeniedException) -> {
      ErrorCode errorCode = ErrorCode.UNAUTHORIZED;

      response.setStatus(errorCode.getStatusCode().value());
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);

      ApiResponse apiResponse = ApiResponse.builder()
        .code(errorCode.getCode())
        .message(errorCode.getMessage())
        .build();

      ObjectMapper objectMapper = new ObjectMapper();

      response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
      response.flushBuffer();
    });
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    config.setAllowedOrigins(List.of(
      "http://localhost:3000",          // React local
      "http://localhost:5173",          // Vite local (nếu dùng)
      "https://*.up.railway.app"        // Swagger + prod
    ));

    config.setAllowedMethods(List.of(
      "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
    ));

    config.setAllowedHeaders(List.of("*"));

    // ❗ Swagger + JWT + OAuth2 → PHẢI false nếu origins dùng wildcard
    config.setAllowCredentials(false);

    UrlBasedCorsConfigurationSource source =
      new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
