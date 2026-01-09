package com.exe.unihome.auth.controller;

import com.exe.unihome.auth.model.ChangePasswordRequest;
import com.exe.unihome.auth.model.UserCreateRequest;
import com.exe.unihome.auth.model.UserRequest;
import com.exe.unihome.auth.model.UserResponse;
import com.exe.unihome.auth.service.UserService;
import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.persistence.entity.identityAndAuth.Status;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
  private final UserService userService;

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable String id) {
    return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
      .code(200)
      .message("User retrieved successfully")
      .data(userService.getUserById(id))
      .build());
  }


  @GetMapping("/getAll")
  public ResponseEntity<ApiResponse<?>> searchUsers(
    @RequestParam(required = false) String email,
    @ParameterObject
    @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

    // If email parameter is provided, search with pagination
    if (email != null && !email.isBlank()) {
      return ResponseEntity.ok(ApiResponse.<Page<UserResponse>>builder()
        .code(0)
        .message("Users found successfully")
        .data(userService.searchUsers(email, pageable))
        .build());
    }

    // If no email parameter, return paginated users
    return ResponseEntity.ok(ApiResponse.<Page<UserResponse>>builder()
      .code(0)
      .message("Users retrieved successfully with pagination")
      .data(userService.getAllUsersPaginated(pageable))
      .build());
  }

  @PostMapping
  public ResponseEntity<ApiResponse<UserResponse>> createUser(
    @RequestBody UserCreateRequest userCreateRequest) {
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.<UserResponse>builder()
        .code(0)
        .message("User created successfully")
        .data(userService.createUser(userCreateRequest))
        .build());
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse<UserResponse>> updateUser(
    @PathVariable String id,
    @RequestBody UserRequest userRequest) {
    return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
      .code(200)
      .message("User updated successfully")
      .data(userService.updateUser(id, userRequest))
      .build());
  }

  @PostMapping("/{id}/status")
  public ResponseEntity<ApiResponse<UserResponse>> updateUserStatus(@PathVariable String id, Status status) {
    return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
      .code(200)
      .message("User banned successfully")
      .data(userService.updateUserStatus(id, status))
      .build());
  }

  @PutMapping("/{id}/change-password")
  public ResponseEntity<ApiResponse<UserResponse>> changePassword(
    @PathVariable String id,
    @RequestBody ChangePasswordRequest request) {
    return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
      .code(200)
      .message("Password changed successfully")
      .data(userService.changePassword(id, request))
      .build());
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable String id) {
    userService.deleteUser(id);
    return ResponseEntity.status(HttpStatus.OK)
      .body(ApiResponse.<Void>builder()
        .code(200)
        .message("User deleted successfully")
        .build());
  }
}

