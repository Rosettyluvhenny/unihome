package com.exe.unihome.postAndSubscription.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.postAndComment.request.CreatePostDetailRequest;
import com.exe.unihome.dto.postAndComment.request.CreatePostRequest;
import com.exe.unihome.dto.postAndComment.request.UpdatePostRequest;
import com.exe.unihome.dto.postAndComment.response.PostResponse;
import com.exe.unihome.mapper.PostDetailMapper;
import com.exe.unihome.mapper.PostMapper;
import com.exe.unihome.persistence.entity.Category;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import com.exe.unihome.persistence.entity.postAndComment.Post;
import com.exe.unihome.persistence.entity.postAndComment.PostDetail;
import com.exe.unihome.persistence.entity.postAndComment.PostStatus;
import com.exe.unihome.persistence.entity.postAndComment.PostUserBoostStatus;
import com.exe.unihome.persistence.repository.*;
import com.exe.unihome.postAndSubscription.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

  private final PostRepository postRepository;
  private final PostDetailRepository postDetailRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;
  private final PostMapper postMapper;
  private final PostDetailMapper postDetailMapper;
  private final PostUserBoostRepository postUserBoostRepository;

  @Override
  @Transactional
  public PostResponse createPost(CreatePostRequest request) {
    String userId = getUserId();
    // Validate user exists
    User user = userRepository.findById(userId)
      .orElseThrow(() -> {
        log.error("User not found: {}", userId);
        return new AppException(ErrorCode.USER_NOT_FOUND);
      });

    // Validate category exists
    Category category = categoryRepository.findById(request.getCategoryId())
      .orElseThrow(() -> {
        log.error("Category not found: {}", request.getCategoryId());
        return new AppException(ErrorCode.CATEGORY_NOT_FOUND);
      });


    // Create post
    Post post = Post.builder()
      .title(request.getTitle())
      .price(request.getPrice())
      .status(request.getStatus())
      .user(user)
      .category(category)
      .build();

    log.info("Post created with ID: {}", post.getId());

    // Create post detail if provided
    List<CreatePostDetailRequest> postDetailRq = request.getPostDetail();
    List<PostDetail> postDetails = new ArrayList<>();
    //persist if PostDetail availalbe
    if (postDetailRq != null) {
      for (CreatePostDetailRequest postDetailRequest : postDetailRq) {
        PostDetail postDetail = postDetailMapper.toEntity(postDetailRequest);
        postDetail.setPost(post);
        postDetails.add(postDetail);
      }
      post.setPostDetail(postDetails);
      post = postRepository.save(post);
    }

    return postMapper.toResponse(post);
  }

  @Override
  @Transactional(readOnly = true)
  public PostResponse getPostById(String id) {
    log.info("Fetching post by ID: {}", id);
    Post post = postRepository.findByIdAndStatusWithDetails(id, PostStatus.ACTIVE)
      .orElseThrow(() -> {
        log.error("Post not found: {}", id);
        return new AppException(ErrorCode.POST_NOT_FOUND);
      });
    //add user summary and comment pagination
    return postMapper.toResponse(post);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PostResponse> getAllPosts(Pageable pageable) {
    var result = postRepository.searchPost(null, pageable).map(postMapper::toResponse);
    result.stream().forEach(post -> {
      var activeBoostUsage = postUserBoostRepository.findAllByPostIdAndStatus(post.getId(), PostUserBoostStatus.ACTIVE);
      if (!activeBoostUsage.isEmpty()) {
        post.setBoost(true);
      }
    });
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PostResponse> getPostsByUserId(String userId, Pageable pageable) {
    log.info("Fetching posts for user: {}", userId);
    return postRepository.findByUserIdAndStatus(userId, PostStatus.ACTIVE, pageable)
      .map(postMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PostResponse> getPostsByCategory(UUID categoryId, Pageable pageable) {
    log.info("Fetching posts for category: {}", categoryId);

    var result = postRepository.findByCategoryId(categoryId, pageable)
      .map(postMapper::toResponse);
    result.stream().forEach(post -> {
      var activeBoostUsage = postUserBoostRepository.findAllByPostIdAndStatus(post.getId(), PostUserBoostStatus.ACTIVE);
      if (!activeBoostUsage.isEmpty()) {
        post.setBoost(true);
      }
    });
    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PostResponse> searchPostsByTitle(String title, Pageable pageable) {
    log.info("Searching posts by title: {}", title);
    var result = postRepository.searchPost(title.trim(), pageable).map(postMapper::toResponse);
    result.stream().forEach(post -> {
      var activeBoostUsage = postUserBoostRepository.findAllByPostIdAndStatus(post.getId(), PostUserBoostStatus.ACTIVE);
      if (!activeBoostUsage.isEmpty()) {
        post.setBoost(true);
      }
    });
    return result;
  }

  @Override
  @Transactional
  public PostResponse updatePost(String id, UpdatePostRequest request) {
    Post post = postRepository.findByIdAndStatus(id, PostStatus.ACTIVE)
      .orElseThrow(() -> {
        log.error("Post not found: {}", id);
        return new AppException(ErrorCode.POST_NOT_FOUND);
      });
    // Verify ownership
    checkOwnerShip(post.getUserId());

    // Update fields
    if (request.getTitle() != null) {
      post.setTitle(request.getTitle());
    }
    if (request.getPrice() != null) {
      post.setPrice(new BigDecimal(request.getPrice()));
    }
    if (request.getCategoryId() != null) {
      Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new AppException(ErrorCode.CATEGORY_NOT_FOUND));
      post.setCategory(category);
    }

    post = postRepository.save(post);
    log.info("Post updated: {}", id);
    return postMapper.toResponse(post);
  }

  @Override
  @Transactional
  public void disabledPost(String id) {

    Post post = postRepository.findById(id)
      .orElseThrow(() -> {
        log.error("Post not found: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });
    checkOwnerShip(post.getUserId());
    // Verify ownership
    post.setStatus(PostStatus.DELETED);
    postRepository.save(post);
  }

  private void checkOwnerShip(String userId) {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    boolean isAdmin = authentication.getAuthorities().stream()
      .anyMatch(a ->
        a.getAuthority().equals("ADMIN"));
    if (!isAdmin)
      if (!authentication.getName().equals(userId)) {
        throw new AppException(ErrorCode.UNAUTHORIZED);
      }
  }

  private String getUserId() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return authentication.getName();
  }
}

