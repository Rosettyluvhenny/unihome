package com.exe.unihome.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final ObjectProvider<OAuth2SuccessHandler> oAuth2SuccessHandlerProvider;


    @Bean
    public OAuth2UserService<?, OAuth2User> oauth2UserService() {
        return new DefaultOAuth2UserService();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/auth/**",
                                "/oauth2/**",
                                "/login/oauth2/**",
                                "/actuator/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**")
                        .permitAll()
                        .anyRequest().authenticated())
                .oauth2Login(oauth2 -> {
                  oauth2
                    .userInfoEndpoint(userInfo ->
                        userInfo.userService((OAuth2UserService<OAuth2UserRequest, OAuth2User>) oauth2UserService()));
                  OAuth2SuccessHandler oAuth2SuccessHandler = oAuth2SuccessHandlerProvider.getIfAvailable();
                  if(oAuth2SuccessHandler!=null)
                    oauth2.successHandler(oAuth2SuccessHandler);
                })
                .httpBasic(Customizer.withDefaults());
        return http.build();
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
