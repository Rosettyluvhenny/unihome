package com.exe.unihome.auth.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegistrationRequest {
  @NotBlank(message = "Full name is required")
  private String fullName;

  @Email(message = "Email is invalid")
  @NotBlank(message = "Email is required")
  private String email;

  @NotBlank(message = "Password is required")
  private String password;

  @NotBlank()
  @Pattern(regexp = "^0\\d{9}$")
  private String phone;
}
