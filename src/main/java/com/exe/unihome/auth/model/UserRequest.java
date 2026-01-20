package com.exe.unihome.auth.model;

import com.exe.unihome.persistence.entity.identityAndAuth.Status;
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
  private Status status;
}
