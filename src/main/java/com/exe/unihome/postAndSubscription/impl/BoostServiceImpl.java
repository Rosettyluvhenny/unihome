package com.exe.unihome.postAndSubscription.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.subscription.request.CreateBoostRequest;
import com.exe.unihome.dto.subscription.request.UpdateBoostRequest;
import com.exe.unihome.dto.subscription.response.BoostResponse;
import com.exe.unihome.mapper.BoostMapper;
import com.exe.unihome.persistence.entity.subscription.Boost;
import com.exe.unihome.persistence.entity.subscription.BoostStatus;
import com.exe.unihome.persistence.repository.BoostRepository;
import com.exe.unihome.postAndSubscription.BoostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoostServiceImpl implements BoostService {

  private final BoostRepository boostRepository;
  private final BoostMapper boostMapper;

  @Override
  @Transactional
  public BoostResponse createBoost(CreateBoostRequest request) {
    log.info("Creating boost: {}", request.getName());

    Boost boost = Boost.builder()
      .name(request.getName())
      .price(request.getPrice())
      .duration(request.getDuration() != null ? request.getDuration() : 0)
      .status(request.getStatus() != null ? request.getStatus() : BoostStatus.ACTIVE)
      .build();

    boost = boostRepository.save(boost);
    log.info("Boost created with ID: {}", boost.getId());
    return boostMapper.toResponse(boost);
  }

  @Override
  @Transactional(readOnly = true)
  public BoostResponse getBoostById(String id) {
    log.info("Fetching boost by ID: {}", id);
    Boost boost = boostRepository.findById(id)
      .orElseThrow(() -> {
        log.error("Boost not found: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });
    return boostMapper.toResponse(boost);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<BoostResponse> getAllBoosts(Pageable pageable) {
    log.info("Fetching all boosts");
    return boostRepository.findAll(pageable)
      .map(boostMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<BoostResponse> getBoostsByStatus(BoostStatus status, Pageable pageable) {
    log.info("Fetching boosts with status: {}", status);
    return boostRepository.findByStatus(status, pageable)
      .map(boostMapper::toResponse);
  }

  @Override
  @Transactional
  public BoostResponse updateBoost(String id, UpdateBoostRequest request) {
    log.info("Updating boost: {}", id);

    Boost boost = boostRepository.findById(id)
      .orElseThrow(() -> {
        log.error("Boost not found: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });

    if (request.getName() != null) {
      boost.setName(request.getName());
    }
    if (request.getPrice() != null) {
      boost.setPrice(request.getPrice());
    }
    if (request.getDuration() != null) {
      boost.setDuration(request.getDuration());
    }
    if (request.getStatus() != null) {
      boost.setStatus(request.getStatus());
    }

    boost = boostRepository.save(boost);
    log.info("Boost updated: {}", id);
    return boostMapper.toResponse(boost);
  }

  @Override
  @Transactional
  public void deleteBoost(String id) {
    log.info("Deleting boost: {}", id);

    Boost boost = boostRepository.findById(id)
      .orElseThrow(() -> {
        log.error("Boost not found: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });

    boostRepository.delete(boost);
    log.info("Boost deleted: {}", id);
  }
}

