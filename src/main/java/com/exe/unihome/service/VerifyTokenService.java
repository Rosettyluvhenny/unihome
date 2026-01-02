package com.exe.unihome.service;

import com.exe.unihome.entity.identityAndAuth.VerifyToken;

public interface VerifyTokenService {
  String issueVerifyToken(String userId);

  void verifyToken(String verifyTokenString);

  VerifyToken getValidVerifyToken(String tokenId, String secret);
}

