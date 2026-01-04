package com.exe.unihome.auth.service;

import com.exe.unihome.persistence.entity.identityAndAuth.VerifyToken;

public interface VerifyTokenService {
  String issueVerifyToken(String userId);

  void verifyToken(String verifyTokenString);

  VerifyToken getValidVerifyToken(String tokenId, String secret);
}

