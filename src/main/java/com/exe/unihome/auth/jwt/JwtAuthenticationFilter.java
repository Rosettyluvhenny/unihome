package com.exe.unihome.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtUtil jwtUtil;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
    throws ServletException, IOException {
    try {
      String token = extractTokenFromRequest(request);
      if (token != null && jwtUtil.validateToken(token)) {
        String userId = jwtUtil.extractUserId(token);
        String role = jwtUtil.extractRole(token);

        UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(
            userId,
            null,
            Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role))
          );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.debug("Set Spring Security authentication for userId: {}", userId);
      }
    } catch (Exception e) {
      log.debug("Could not set user authentication in security context", e);
    }

    filterChain.doFilter(request, response);
  }

  private String extractTokenFromRequest(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}

