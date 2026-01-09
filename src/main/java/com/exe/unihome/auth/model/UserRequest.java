package com.exe.unihome.auth.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {
  private String fullName;
  private String email;
  private String address;
  private String phone;
  private String image;
}
