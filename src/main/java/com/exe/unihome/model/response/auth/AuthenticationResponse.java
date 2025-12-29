package com.exe.unihome.model.response.auth;

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
}
