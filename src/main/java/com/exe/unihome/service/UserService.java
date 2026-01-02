package com.exe.unihome.service;

import com.exe.unihome.entity.identityAndAuth.User;
import com.exe.unihome.model.request.auth.RegistrationRequest;
import com.exe.unihome.model.response.UserResponse;
import com.exe.unihome.model.response.auth.RegistrationResponse;

public interface UserService {
  RegistrationResponse register(RegistrationRequest request);

  UserResponse registerGoogleUser(String email, String fullName);

  User getOrCreateGoogleUser(String email, String fullName);
}
