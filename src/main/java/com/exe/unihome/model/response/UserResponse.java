package com.exe.unihome.model.response;

import com.exe.unihome.entity.identityAndAuth.RoleName;
import com.exe.unihome.entity.identityAndAuth.Status;
import lombok.Data;

@Data
public class UserResponse {
  private String id;
  private String fullName;
  private String email;
  private Status status;
  private RoleName role;
  private String address;
}
