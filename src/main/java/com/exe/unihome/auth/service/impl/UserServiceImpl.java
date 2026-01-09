package com.exe.unihome.auth.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.auth.mapper.UserMapper;
import com.exe.unihome.mail.model.MailJob;
import com.exe.unihome.auth.model.RegistrationRequest;
import com.exe.unihome.auth.model.UserResponse;
import com.exe.unihome.auth.model.RegistrationResponse;
import com.exe.unihome.persistence.entity.identityAndAuth.RoleName;
import com.exe.unihome.persistence.entity.identityAndAuth.Status;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import com.exe.unihome.persistence.repository.UserRepository;
import com.exe.unihome.mail.service.MailQueueService;
import com.exe.unihome.mail.service.MailService;
import com.exe.unihome.auth.service.UserService;
import com.exe.unihome.auth.service.VerifyTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserMapper userMapper;
  private final VerifyTokenService verifyTokenService;
  private final MailService mailService;
  private final MailQueueService mailQueueService;

  @Override
  @Transactional
  public RegistrationResponse register(RegistrationRequest request) {
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
    }

    User user = User.builder()
      .id(UUID.randomUUID().toString())
      .fullName(request.getFullName())
      .email(request.getEmail())
      .password(passwordEncoder.encode(request.getPassword()))
      .status(Status.UNACTIVE)
      .role(RoleName.CUSTOMER)
      .build();
    User savedUser = userRepository.save(user);

    // Generate verify token
    String rawVerifyToken = verifyTokenService.issueVerifyToken(savedUser.getId());

    // Queue Mail job
    MailJob mailJob = MailJob.builder()
      .jobId(UUID.randomUUID().toString())
      .type("VERIFY_EMAIL")
      .toEmail(savedUser.getEmail())
      .fullName(savedUser.getFullName())
      .verifyToken(rawVerifyToken)
      .retryCount(0)
      .createdAt(Instant.now())
      .build();

    mailQueueService.enqueue(mailJob);

    return RegistrationResponse.builder()
      .user(userMapper.toResponse(savedUser))
      .message("Registration successful. Please check your email to verify your account and activate access.")
      .build();
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
      .id(UUID.randomUUID().toString())
      .fullName(resolvedName)
      .email(email)
      .password(passwordEncoder.encode(UUID.randomUUID().toString()))
      .status(Status.ACTIVE)
      .role(RoleName.CUSTOMER)
      .build();
    return userRepository.save(user);
  }
}




