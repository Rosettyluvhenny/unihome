package com.exe.unihome.postAndSubscription.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.postAndComment.request.CreatePostUserBoostRequest;
import com.exe.unihome.dto.postAndComment.request.UpdatePostUserBoostRequest;
import com.exe.unihome.dto.postAndComment.response.PostUserBoostResponse;
import com.exe.unihome.mapper.PostUserBoostMapper;
import com.exe.unihome.persistence.entity.postAndComment.Post;
import com.exe.unihome.persistence.entity.postAndComment.PostUserBoost;
import com.exe.unihome.persistence.entity.postAndComment.PostUserBoostStatus;
import com.exe.unihome.persistence.entity.subscription.UserBoost;
import com.exe.unihome.persistence.repository.PostRepository;
import com.exe.unihome.persistence.repository.PostUserBoostRepository;
import com.exe.unihome.persistence.repository.UserBoostRepository;
import com.exe.unihome.postAndSubscription.PostUserBoostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    if (!post.getStatus().equals(PostUserBoostStatus.ACTIVE))
      throw new AppException(ErrorCode.INVALID_POST);
    checkPostOwnerShip(post);
    // Validate user boost exists
    UserBoost userBoost = userBoostRepository.findById(request.getUserBoostId())
      .orElseThrow(() -> {
        log.error("User boost not found: {}", request.getUserBoostId());
        return new AppException(ErrorCode.INVALID_REQUEST);
      });
    checkUserBoostOwnerShip(userBoost);
    LocalDateTime endTime = normalizeLocalDateTime(request.getEndTime());
    LocalDateTime startTime = normalizeLocalDateTime(request.getStartTime());
    checkLocalDateTime(endTime);
    checkLocalDateTime(startTime);
    checkTimeOverLap(request.getPostId(), startTime, endTime);
    long check = Duration.between(startTime, endTime).toMinutes();
    if (check <= 0 || check > userBoost.getTimeRemain()) {
      throw new AppException(ErrorCode.INVALID_BOOST_USAGE_TIME);
    }
    PostUserBoost postUserBoost = PostUserBoost.builder()
      .post(post)
      .userBoost(userBoost)
      .startTime(startTime)
      .endTime(endTime)
      .status(PostUserBoostStatus.SCHEDULE)
      .build();
    int duration = userBoost.getTimeRemain();
    userBoost.setTimeRemain(duration - (int) check);
    postUserBoost = postUserBoostRepository.save(postUserBoost);
    log.info("Post user boost created with ID: {}", postUserBoost.getId());
    return postUserBoostMapper.toResponse(postUserBoost);
  }

  private void checkTimeOverLap(String postId, LocalDateTime startTime, LocalDateTime endTime) {
    List<PostUserBoost> currentBoosts =
      Stream.concat(
        postUserBoostRepository
          .findAllByPostIdAndStatus(postId, PostUserBoostStatus.ACTIVE).stream(),
        postUserBoostRepository
          .findAllByPostIdAndStatus(postId, PostUserBoostStatus.SCHEDULE).stream()
      ).toList();
    for (PostUserBoost currentBoost : currentBoosts) {
      if (startTime.isBefore(currentBoost.getEndTime())
        && endTime.isAfter(currentBoost.getStartTime())) {
        throw new AppException(ErrorCode.INVALID_BOOST_USAGE_TIME);
      }
    }
  }

  @Override
  @Transactional(readOnly = true)
  public PostUserBoostResponse getPostUserBoostById(String id) {
    log.info("Fetching post user boost by ID: {}", id);
    PostUserBoost postUserBoost = postUserBoostRepository.findById(id)
      .orElseThrow(() -> {
        log.error("Post user boost not found: {}", id);
        return new AppException(ErrorCode.BOOST_USAGE_NOT_FOUND);
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
  public List<PostUserBoostResponse> getPostUserBoostsByUserBoostId(String userBoostId, Pageable pageable) {
    log.info("Fetching post user boosts for user boost: {}", userBoostId);
    return postUserBoostRepository.findByUserBoostId(userBoostId, pageable).stream()
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
  public PostUserBoostResponse updatePostUserBoost(String id, UpdatePostUserBoostRequest request) {
    log.info("Updating post user boost: {}", id);
    PostUserBoost postUserBoost = postUserBoostRepository.findById(id)
      .orElseThrow(() -> {
        log.error("Post user boost not found: {}", id);
        return new AppException(ErrorCode.BOOST_SUBSCRIPTION_NOT_FOUND);
      });
    if (postUserBoost.getStatus().equals(PostUserBoostStatus.CANCELLED)
      || postUserBoost.getStatus().equals(PostUserBoostStatus.FINISH))
      throw new AppException(ErrorCode.BOOST_USAGE_UNUPDATABLE);
    checkPostOwnerShip(postUserBoost.getPost());
    LocalDateTime endTime = normalizeLocalDateTime(request.getEndTime());
    log.info("endtime: {}", endTime);
    checkLocalDateTime(endTime);
    LocalDateTime currentEndTime = normalizeLocalDateTime(postUserBoost.getEndTime());
    log.info("currentEndTime: {}", currentEndTime);
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime startTime = normalizeLocalDateTime(postUserBoost.getStartTime());
    if (endTime.getMinute() % 30 != 0 || endTime.isBefore(now) || endTime.isEqual(startTime)) {
      throw new AppException(ErrorCode.INVALID_BOOST_USAGE_TIME);
    }

    int duration = (int) Duration.between(currentEndTime, endTime).toMinutes();
    duration = (duration / 30) * 30;
    log.info("duration: {}", duration);
    UserBoost userBoost = postUserBoost.getUserBoost();
    int remain = userBoost.getTimeRemain();
    if (duration == 0) {
      throw new AppException(ErrorCode.INVALID_BOOST_USAGE_TIME);
    }
    if (duration < 0 && endTime.isBefore(now) && endTime.isEqual(postUserBoost.getStartTime())) {
      throw new AppException(ErrorCode.INVALID_BOOST_USAGE_TIME);

    }
    if (duration > remain) {
      throw new AppException(ErrorCode.INVALID_BOOST_USAGE_TIME);
    }
    userBoost.setTimeRemain(remain - duration);
    postUserBoost.setEndTime(request.getEndTime());

    postUserBoost = postUserBoostRepository.save(postUserBoost);
    log.info("Post user boost updated: {}", id);
    return postUserBoostMapper.toResponse(postUserBoost);
  }

  private LocalDateTime normalizeNow() {
    LocalDateTime now = LocalDateTime.now()
      .withSecond(0)
      .withNano(0);

    return now.withMinute(now.getMinute() < 30 ? 0 : 30);
  }

  private LocalDateTime normalizeLocalDateTime(LocalDateTime localDateTime) {
    LocalDateTime result = localDateTime.withSecond(0)
      .withNano(0);
    return result.withMinute(result.getMinute() < 30 ? 0 : 30);
  }

  @Override
  @Transactional
  @Scheduled(fixedRate = 30000)
//  @Scheduled(cron = "0 0,30 * * * *", zone = "Asia/Ho_Chi_Minh")
  public void updateBoostUsageStatus() {

    LocalDateTime now = normalizeNow();

    int started = postUserBoostRepository.markOnGoing(now);
    int finished = postUserBoostRepository.markFinished(now);

    log.info("BoostUsage status updated - started: {}, finished: {}",
      started, finished);
  }

  @Override
  @Transactional
  public PostUserBoostResponse cancelPostUserBoost(String id) {
    PostUserBoost postUserBoost = postUserBoostRepository.findById(id)
      .orElseThrow(() -> new AppException(ErrorCode.BOOST_USAGE_NOT_FOUND));
    if (postUserBoost.getStatus() == PostUserBoostStatus.FINISH
      || postUserBoost.getStatus() == PostUserBoostStatus.CANCELLED) {
      throw new AppException(ErrorCode.UNABLE_TO_CANCEL);
    }
    Post post = postUserBoost.getPost();
    checkPostOwnerShip(post);
    LocalDateTime now = normalizeNow();
    LocalDateTime endTime = normalizeLocalDateTime(postUserBoost.getEndTime());
    LocalDateTime startTime = normalizeLocalDateTime(postUserBoost.getStartTime());
    UserBoost userBoost = postUserBoost.getUserBoost();
    int timeRemain = userBoost.getTimeRemain();
    //the BoostUsage not activate yet cancel right away
    if (!startTime.isBefore(now)) {
      int duration = (int) Duration.between(startTime, endTime).toMinutes();
      postUserBoost.setStatus(PostUserBoostStatus.CANCELLED);
      userBoost.setTimeRemain(timeRemain + duration);
      postUserBoost.setEndTime(startTime);
    } else {
      // refund multiple of 30
      int remainDuration = (int) Duration.between(now, endTime).toMinutes();
      if (remainDuration <= 30)
        throw new AppException(ErrorCode.UNABLE_TO_CANCEL);
      else {
        int remainder = remainDuration / 30;
        int leftOver = remainDuration % 30;
        postUserBoost.setEndTime(endTime.minusMinutes(30 * remainder));
        userBoost.setTimeRemain(timeRemain + (30 * remainder));
        log.info("cancel in minute: {}", leftOver);
      }
      postUserBoostRepository.save(postUserBoost);
    }

    return postUserBoostMapper.toResponse(postUserBoost);
  }


  private void checkLocalDateTime(LocalDateTime localDateTime) {
    if (localDateTime.isBefore(normalizeNow())) {
      throw new AppException(ErrorCode.INVALID_BOOST_USAGE_TIME);
    }
  }


  private void checkPostOwnerShip(Post post) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean isAdmin = authentication.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    if (isAdmin) {
      return;
    }
    String userId = authentication.getName();
    String postOwnerId = post.getUser().getId();
    if (!postOwnerId.equals(userId)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
  }

  private void checkUserBoostOwnerShip(UserBoost userBoost) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean isAdmin = authentication.getAuthorities().stream()
      .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    if (isAdmin) {
      return;
    }
    String userId = authentication.getName();

    String boostOwnerId = userBoost.getUserId();
    if (!boostOwnerId.equals(userId)) {
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }
  }
}

