package com.exe.unihome.service.impl;

import com.exe.unihome.AppException;
import com.exe.unihome.entity.RoleName;
import com.exe.unihome.entity.Status;
import com.exe.unihome.entity.User;
import com.exe.unihome.exception.ErrorCode;
import com.exe.unihome.mapper.UserMapper;
import com.exe.unihome.model.request.auth.RegistrationRequest;
import com.exe.unihome.model.response.UserResponse;
import com.exe.unihome.repository.UserRepository;
import com.exe.unihome.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public UserResponse register(RegistrationRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(Status.ACTIVE)
                .role(RoleName.MEMBER)
                .build();
        return userMapper.toResponse(userRepository.save(user));
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
                .role(RoleName.MEMBER)
                .build();
        return userRepository.save(user);
    }
}
