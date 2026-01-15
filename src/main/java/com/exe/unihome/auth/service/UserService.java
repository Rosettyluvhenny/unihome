package com.exe.unihome.auth.service;

import com.exe.unihome.auth.model.*;
import com.exe.unihome.persistence.entity.identityAndAuth.Status;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
  UserResponse register(RegistrationRequest request);

  UserResponse registerGoogleUser(String email, String fullName);

  User getOrCreateGoogleUser(String email, String fullName);

  UserResponse resendVerification(String email);

  // CRUD Operations
  UserResponse getUserById(String id);

  List<UserResponse> getAllUsers();

  Page<UserResponse> getAllUsersPaginated(Pageable pageable);

  Page<UserResponse> searchUsers(String email, Pageable pageable);

  UserResponse createUser(UserCreateRequest userCreateRequest);

  UserResponse updateUser(String id, UserRequest userRequest);

  UserResponse updateUserStatus(String id, Status status);

  UserResponse changePassword(String userId, ChangePasswordRequest request);

  void deleteUser(String id);
  
  boolean existById(String id);
}
