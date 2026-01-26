package com.exe.unihome.service.postAndSubscription.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.subscription.request.CreateUserBoostRequest;
import com.exe.unihome.dto.subscription.request.UpdateUserBoostRequest;
import com.exe.unihome.dto.subscription.response.UserBoostResponse;
import com.exe.unihome.mapper.UserBoostMapper;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import com.exe.unihome.persistence.entity.subscription.UserBoost;
import com.exe.unihome.persistence.repository.UserBoostRepository;
import com.exe.unihome.persistence.repository.UserRepository;
import com.exe.unihome.service.postAndSubscription.UserBoostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserBoostServiceImpl implements UserBoostService {

  private final UserBoostRepository userBoostRepository;
  private final UserRepository userRepository;
  private final UserBoostMapper userBoostMapper;

  @Override
  @Transactional
  public UserBoostResponse createUserBoost(CreateUserBoostRequest request) {
    log.info("Creating user boost for user: {}", request.getUserId());

    // Validate user exists
    User user = userRepository.findById(request.getUserId())
      .orElseThrow(() -> {
        log.error("User not found: {}", request.getUserId());
        return new AppException(ErrorCode.USER_NOT_FOUND);
      });

    UserBoost userBoost = userBoostMapper.toEntity(request);

    userBoost = userBoostRepository.save(userBoost);
    log.info("User boost created with ID: {}", userBoost.getId());
    return userBoostMapper.toResponse(userBoost);
  }

  @Override
  @Transactional(readOnly = true)
  public UserBoostResponse getUserBoostById(String id) {
    log.info("Fetching user boost by ID: {}", id);
    UserBoost userBoost = userBoostRepository.findById(id)
      .orElseThrow(() -> {
        log.error("User boost not found: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });
    return userBoostMapper.toResponse(userBoost);
  }

  @Override
  @Transactional(readOnly = true)
  public List<UserBoostResponse> getUserBoostsByUserId(String userId) {
    log.info("Fetching user boosts for user: {}", userId);
    return userBoostRepository.findByUserId(userId).stream()
      .map(userBoostMapper::toResponse)
      .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Page<UserBoostResponse> getUserBoostsByUserId(String userId, Pageable pageable) {
    log.info("Fetching user boosts for user: {} with pagination", userId);
    return userBoostRepository.findByUserId(userId, pageable)
      .map(userBoostMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<UserBoostResponse> getUserBoostsByStatus(String status, Pageable pageable) {
    log.info("Fetching user boosts with status: {}", status);
    return userBoostRepository.findByStatus(status, pageable)
      .map(userBoostMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<UserBoostResponse> getAllUserBoosts(Pageable pageable) {
    log.info("Fetching all user boosts");
    return userBoostRepository.findAll(pageable)
      .map(userBoostMapper::toResponse);
  }

  @Override
  @Transactional
  public UserBoostResponse updateUserBoost(String id, UpdateUserBoostRequest request) {
    log.info("Updating user boost: {}", id);

    UserBoost userBoost = userBoostRepository.findById(id)
      .orElseThrow(() -> {
        log.error("User boost not found: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });

    if (request.getPrice() != null) {
      userBoost.setPrice(request.getPrice());
    }
    if (request.getStatus() != null) {
      userBoost.setStatus(request.getStatus());
    }

    userBoost = userBoostRepository.save(userBoost);
    log.info("User boost updated: {}", id);
    return userBoostMapper.toResponse(userBoost);
  }

  @Override
  @Transactional
  public void deleteUserBoost(String id) {
    log.info("Deleting user boost: {}", id);

    UserBoost userBoost = userBoostRepository.findById(id)
      .orElseThrow(() -> {
        log.error("User boost not found: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });

    userBoostRepository.delete(userBoost);
    log.info("User boost deleted: {}", id);
  }
}

