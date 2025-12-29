package com.exe.unihome.service;

import com.exe.unihome.entity.User;
import com.exe.unihome.model.request.auth.RegistrationRequest;
import com.exe.unihome.model.response.UserResponse;

public interface UserService {
    UserResponse register(RegistrationRequest request);

    UserResponse registerGoogleUser(String email, String fullName);

    User getOrCreateGoogleUser(String email, String fullName);
}
