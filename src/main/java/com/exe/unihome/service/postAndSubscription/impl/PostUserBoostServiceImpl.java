package com.exe.unihome.service.postAndSubscription.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.postAndComment.request.CreatePostUserBoostRequest;
import com.exe.unihome.dto.postAndComment.response.PostUserBoostResponse;
import com.exe.unihome.mapper.PostUserBoostMapper;
import com.exe.unihome.persistence.entity.postAndComment.Post;
import com.exe.unihome.persistence.entity.postAndComment.PostUserBoost;
import com.exe.unihome.persistence.entity.subscription.UserBoost;
import com.exe.unihome.persistence.repository.PostRepository;
import com.exe.unihome.persistence.repository.PostUserBoostRepository;
import com.exe.unihome.persistence.repository.UserBoostRepository;
import com.exe.unihome.service.postAndSubscription.PostUserBoostService;
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
public class PostUserBoostServiceImpl implements PostUserBoostService {

  private final PostUserBoostRepository postUserBoostRepository;
  private final PostRepository postRepository;
  private final UserBoostRepository userBoostRepository;
  private final PostUserBoostMapper postUserBoostMapper;

  @Override
  @Transactional
  public PostUserBoostResponse createPostUserBoost(CreatePostUserBoostRequest request) {
    log.info("Creating post user boost for post: {} with user boost: {}", request.getPostId(), request.getUserBoostId());

    // Validate post exists
    Post post = postRepository.findById(request.getPostId())
      .orElseThrow(() -> {
        log.error("Post not found: {}", request.getPostId());
        return new AppException(ErrorCode.INVALID_REQUEST);
      });

    // Validate user boost exists
    UserBoost userBoost = userBoostRepository.findById(request.getUserBoostId())
      .orElseThrow(() -> {
        log.error("User boost not found: {}", request.getUserBoostId());
        return new AppException(ErrorCode.INVALID_REQUEST);
      });

    PostUserBoost postUserBoost = PostUserBoost.builder()
      .post(post)
      .userBoost(userBoost)
      .startTime(request.getStartTime())
      .endTime(request.getEndTime())
      .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
      .build();

    postUserBoost = postUserBoostRepository.save(postUserBoost);
    log.info("Post user boost created with ID: {}", postUserBoost.getId());
    return postUserBoostMapper.toResponse(postUserBoost);
  }

  @Override
  @Transactional(readOnly = true)
  public PostUserBoostResponse getPostUserBoostById(String id) {
    log.info("Fetching post user boost by ID: {}", id);
    PostUserBoost postUserBoost = postUserBoostRepository.findById(id)
      .orElseThrow(() -> {
        log.error("Post user boost not found: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });
    return postUserBoostMapper.toResponse(postUserBoost);
  }

  @Override
  @Transactional(readOnly = true)
  public List<PostUserBoostResponse> getPostUserBoostsByPostId(String postId) {
    log.info("Fetching post user boosts for post: {}", postId);
    return postUserBoostRepository.findByPostId(postId).stream()
      .map(postUserBoostMapper::toResponse)
      .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public List<PostUserBoostResponse> getPostUserBoostsByUserBoostId(String userBoostId) {
    log.info("Fetching post user boosts for user boost: {}", userBoostId);
    return postUserBoostRepository.findByUserBoostId(userBoostId).stream()
      .map(postUserBoostMapper::toResponse)
      .collect(Collectors.toList());
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PostUserBoostResponse> getPostUserBoostsByStatus(String status, Pageable pageable) {
    log.info("Fetching post user boosts with status: {}", status);
    return postUserBoostRepository.findByStatus(status, pageable)
      .map(postUserBoostMapper::toResponse);
  }

  @Override
  @Transactional
  public PostUserBoostResponse updatePostUserBoost(String id, CreatePostUserBoostRequest request) {
    log.info("Updating post user boost: {}", id);

    PostUserBoost postUserBoost = postUserBoostRepository.findById(id)
      .orElseThrow(() -> {
        log.error("Post user boost not found: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });

    if (request.getStartTime() != null) {
      postUserBoost.setStartTime(request.getStartTime());
    }
    if (request.getEndTime() != null) {
      postUserBoost.setEndTime(request.getEndTime());
    }
    if (request.getStatus() != null) {
      postUserBoost.setStatus(request.getStatus());
    }

    postUserBoost = postUserBoostRepository.save(postUserBoost);
    log.info("Post user boost updated: {}", id);
    return postUserBoostMapper.toResponse(postUserBoost);
  }

  @Override
  @Transactional
  public void deletePostUserBoost(String id) {
    log.info("Deleting post user boost: {}", id);

    PostUserBoost postUserBoost = postUserBoostRepository.findById(id)
      .orElseThrow(() -> {
        log.error("Post user boost not found: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });

    postUserBoostRepository.delete(postUserBoost);
    log.info("Post user boost deleted: {}", id);
  }
}

