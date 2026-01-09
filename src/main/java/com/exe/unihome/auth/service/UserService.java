package com.exe.unihome.auth.service;

import com.exe.unihome.auth.model.RegistrationRequest;
import com.exe.unihome.auth.model.UserResponse;
import com.exe.unihome.auth.model.RegistrationResponse;
import com.exe.unihome.persistence.entity.identityAndAuth.User;

public interface UserService {
  RegistrationResponse register(RegistrationRequest request);

  UserResponse registerGoogleUser(String email, String fullName);

  User getOrCreateGoogleUser(String email, String fullName);
}
