package com.exe.unihome.auth.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthenticationResponse {
  private String token;
  private String userId;
  private List<String> roles;
  private String fullName;
  private String image;
}
