package com.exe.unihome.auth.jwt;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.config.JwtProperties;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {
  private final JwtProperties jwtProperties;


  public String extractUserId(String token) {
    try {
      JWTClaimsSet claims = parseToken(token);
      return claims.getStringClaim("userId");
    } catch (JwtException e) {
      throw new AppException(ErrorCode.INVALID_TOKEN);
    } catch (ParseException e) {
      throw new AppException(ErrorCode.TOKEN_PARSE_ERROR);
    }
  }

  public String extractRole(String token) {
    try {
      JWTClaimsSet claims = parseToken(token);
      return claims.getStringClaim("role");
    } catch (JwtException e) {
      throw new AppException(ErrorCode.INVALID_TOKEN);
    } catch (ParseException e) {
      throw new AppException(ErrorCode.TOKEN_PARSE_ERROR);
    }
  }

  public boolean validateToken(String token) {
    try {
      JWTClaimsSet claims = parseToken(token);

      // Verify signature
      SignedJWT signedJWT = SignedJWT.parse(token);
      JWSVerifier verifier = new MACVerifier(jwtProperties.getSignerKey().getBytes());

      if (!signedJWT.verify(verifier)) {
        return false;
      }

      // Check expiration - return FALSE if expired
      Date expirationTime = claims.getExpirationTime();
      return expirationTime != null && expirationTime.after(new Date());

    } catch (Exception e) {
      return false;
    }
  }


  private JWTClaimsSet parseToken(String token) {
    try {
      SignedJWT signedJWT = SignedJWT.parse(token);
      return signedJWT.getJWTClaimsSet();
    } catch (ParseException e) {
      throw new AppException(ErrorCode.INVALID_TOKEN);
    }
  }

}

