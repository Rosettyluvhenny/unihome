package com.exe.unihome.auth.service.impl;

import com.exe.unihome.auth.mapper.UserMapper;
import com.exe.unihome.auth.model.*;
import com.exe.unihome.auth.service.UserService;
import com.exe.unihome.auth.service.VerifyTokenService;
import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.mail.model.MailJob;
import com.exe.unihome.mail.service.MailQueueService;
import com.exe.unihome.persistence.entity.identityAndAuth.RoleName;
import com.exe.unihome.persistence.entity.identityAndAuth.Status;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import com.exe.unihome.persistence.entity.identityAndAuth.VerifyToken;
import com.exe.unihome.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final VerifyTokenService verifyTokenService;
  private final MailQueueService mailQueueService;

  @Override
  @Transactional
  public UserResponse register(RegistrationRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
    if (userRepository.existsByPhone(request.getPhone())) {
      throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
    }
    User user = User.builder()
      .fullName(request.getFullName())
      .email(request.getEmail())
      .password(passwordEncoder.encode(request.getPassword()))
      .phone(request.getPhone())
      .status(Status.UNACTIVE)
      .role(RoleName.CUSTOMER)
      .build();
    User savedUser = userRepository.save(user);

    // Generate verify token
    String rawVerifyToken = verifyTokenService.issueVerifyToken(savedUser.getId());

    // Queue Mail job
    return verificationMailQueue(rawVerifyToken, user);

  }

  @Override
  public UserResponse registerGoogleUser(String email, String fullName) {
    return userMapper.toResponse(getOrCreateGoogleUser(email, fullName));
  }

  @Override
  public User getOrCreateGoogleUser(String email, String fullName) {
    Optional<User> existingUser = userRepository.findByEmail(email);
    if (existingUser.isPresent()) {
      return existingUser.get();
    }

    String resolvedName = fullName != null && !fullName.isBlank() ? fullName : email;
    User user = User.builder()
      .fullName(resolvedName)
      .email(email)
      .password(passwordEncoder.encode(UUID.randomUUID().toString()))
      .status(Status.ACTIVE)
      .role(RoleName.CUSTOMER)
      .build();
    return userRepository.save(user);
  }

  public UserResponse resendVerification(String email) {
    User user = userRepository.getByEmail(email.toLowerCase()).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    if (user.getStatus().equals(Status.ACTIVE)) {
      throw new AppException(ErrorCode.USER_VERIFIED);
    }
    VerifyToken token = verifyTokenService.findValidVerifyTokenByUserId(user.getId());
    String rawVerifyToken;
    if (token != null) {
      throw new AppException(ErrorCode.ALREADY_SENT);
    }
    rawVerifyToken = verifyTokenService.issueVerifyToken(user.getId());

    return verificationMailQueue(rawVerifyToken, user);

  }

  @Override
  public UserResponse getUserById(String id) {
    User user = userRepository.findById(id)
      .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    return userMapper.toResponse(user);
  }

  @Override
  public List<UserResponse> getAllUsers() {
    return userRepository.findAll()
      .stream()
      .map(userMapper::toResponse)
      .toList();
  }

  @Override
  public Page<UserResponse> getAllUsersPaginated(Pageable pageable) {
    return userRepository.findAll(pageable)
      .map(userMapper::toResponse);
  }

  @Override
  public Page<UserResponse> searchUsers(String email, Pageable pageable) {
    return userRepository.findByEmailContainingIgnoreCase(email, pageable)
      .map(userMapper::toResponse);
  }

  @Override
  @Transactional
  public UserResponse createUser(UserCreateRequest userCreateRequest) {
    if (userRepository.existsByEmail(userCreateRequest.getEmail())) {
      throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }
    if (userCreateRequest.getPhone() != null && userRepository.existsByPhone(userCreateRequest.getPhone())) {
      throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
    }

    // Convert role string to RoleName enum
    RoleName role = RoleName.CUSTOMER;
    if (userCreateRequest.getRole() != null && !userCreateRequest.getRole().isBlank()) {
      try {
        role = RoleName.valueOf(userCreateRequest.getRole().toUpperCase());
      } catch (IllegalArgumentException e) {
        throw new AppException(ErrorCode.INVALID_REQUEST);
      }
    }

    User user = User.builder()
      .fullName(userCreateRequest.getFullName())
      .email(userCreateRequest.getEmail())
      .password(passwordEncoder.encode(userCreateRequest.getPassword()))
      .phone(userCreateRequest.getPhone())
      .address(userCreateRequest.getAddress())
      .image(userCreateRequest.getImage())
      .status(userCreateRequest.getStatus())
      .role(role)
      .build();

    User savedUser = userRepository.save(user);
    return userMapper.toResponse(savedUser);
  }

  @Override
  @Transactional
  public UserResponse updateUser(String id, UserRequest userRequest) {
    User user = userRepository.findById(id)
      .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    if (userRequest.getFullName() != null && !userRequest.getFullName().isBlank()) {
      user.setFullName(userRequest.getFullName());
    }
    if (userRequest.getAddress() != null && !userRequest.getAddress().isBlank()) {
      user.setAddress(userRequest.getAddress());
    }
    if (userRequest.getPhone() != null && !userRequest.getPhone().isBlank()) {
      if (!user.getPhone().equals(userRequest.getPhone()) && userRepository.existsByPhone(userRequest.getPhone())) {
        throw new AppException(ErrorCode.PHONE_ALREADY_EXISTS);
      }
      user.setPhone(userRequest.getPhone());
    }
    if (userRequest.getImage() != null && !userRequest.getImage().isBlank()) {
      user.setImage(userRequest.getImage());
    }

    if (userRequest.getStatus() != null) {
      user.setStatus(userRequest.getStatus());
    }
    User updatedUser = userRepository.save(user);
    return userMapper.toResponse(updatedUser);
  }

  @Override
  @Transactional
  public UserResponse updateUserStatus(String id, Status status) {
    User user = userRepository.findById(id)
      .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    user.setStatus(status);
    User newUser = userRepository.save(user);
    return userMapper.toResponse(newUser);
  }

  @Override
  @Transactional
  public UserResponse changePassword(String userId, ChangePasswordRequest request) {
    if (!request.getNewPassword().equals(request.getConfirmPassword())) {
      throw new AppException(ErrorCode.PASSWORD_MISMATCH);
    }

    User user = userRepository.findById(userId)
      .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

    if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
      throw new AppException(ErrorCode.INVALID_PASSWORD);
    }

    user.setPassword(passwordEncoder.encode(request.getNewPassword()));
    User updatedUser = userRepository.save(user);
    return userMapper.toResponse(updatedUser);
  }

  @Override
  @Transactional
  public void deleteUser(String id) {
    if (!userRepository.existsById(id)) {
      throw new AppException(ErrorCode.USER_NOT_FOUND);
    }
    userRepository.deleteById(id);
  }

  @Override
  public boolean existById(String id) {
    return userRepository.existsById(id);
  }

  private UserResponse verificationMailQueue(String rawVerifyToken, User user) {
    MailJob mailJob = MailJob.builder()
      .jobId(UUID.randomUUID().toString())
      .type("VERIFY_EMAIL")
      .toEmail(user.getEmail())
      .fullName(user.getFullName())
      .verifyToken(rawVerifyToken)
      .retryCount(0)
      .createdAt(LocalDateTime.now())
      .build();

    mailQueueService.enqueue(mailJob);

    return userMapper.toResponse(user);
  }

  public UserSummary toSummary(UserResponse userResponse) {
    return userMapper.ResponsetoSummary(userResponse);
  }
}




