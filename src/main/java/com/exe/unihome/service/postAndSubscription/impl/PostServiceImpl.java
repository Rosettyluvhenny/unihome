package com.exe.unihome.service.postAndSubscription.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.postAndComment.request.CreatePostDetailRequest;
import com.exe.unihome.dto.postAndComment.request.CreatePostRequest;
import com.exe.unihome.dto.postAndComment.request.UpdatePostRequest;
import com.exe.unihome.dto.postAndComment.response.PostResponse;
import com.exe.unihome.mapper.PostMapper;
import com.exe.unihome.persistence.entity.Category;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import com.exe.unihome.persistence.entity.postAndComment.Post;
import com.exe.unihome.persistence.entity.postAndComment.PostDetail;
import com.exe.unihome.persistence.entity.postAndComment.PostStatus;
import com.exe.unihome.persistence.repository.CategoryRepository;
import com.exe.unihome.persistence.repository.PostDetailRepository;
import com.exe.unihome.persistence.repository.PostRepository;
import com.exe.unihome.persistence.repository.UserRepository;
import com.exe.unihome.service.postAndSubscription.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

  private final PostRepository postRepository;
  private final PostDetailRepository postDetailRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;
  private final PostMapper postMapper;

  @Override
  @Transactional
  public PostResponse createPost(CreatePostRequest request, String userId) {
    log.info("Creating post with title: {} for user: {}", request.getTitle(), userId);

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
        return new AppException(ErrorCode.INVALID_REQUEST);
      });

    // Parse status
    PostStatus status;
    try {
      status = PostStatus.valueOf(request.getStatus().toUpperCase());
    } catch (IllegalArgumentException e) {
      log.error("Invalid post status: {}", request.getStatus());
      throw new AppException(ErrorCode.INVALID_REQUEST);
    }

    // Create post
    Post post = Post.builder()
      .title(request.getTitle())
      .price(request.getPrice())
      .status(status)
      .user(user)
      .category(category)
      .build();

    post = postRepository.save(post);
    log.info("Post created with ID: {}", post.getId());

    // Create post detail if provided
    if (request.getPostDetail() != null) {
      CreatePostDetailRequest detailRequest = request.getPostDetail();
      PostDetail postDetail = PostDetail.builder()
        .post(post)
        .description(detailRequest.getDescription())
        .image(detailRequest.getImage())
        .build();
      post.setPostDetail(postDetailRepository.save(postDetail));
      log.info("Post detail created for post ID: {}", post.getId());
    }

    return postMapper.toResponse(post);
  }

  @Override
  @Transactional(readOnly = true)
  public PostResponse getPostById(String id) {
    log.info("Fetching post by ID: {}", id);
    Post post = postRepository.findByIdWithDetails(id)
      .orElseThrow(() -> {
        log.error("Post not found: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });
    return postMapper.toResponse(post);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PostResponse> getAllPosts(Pageable pageable) {
    log.info("Fetching all posts with pagination");
    return postRepository.findAll(pageable)
      .map(postMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PostResponse> getPostsByUserId(String userId, Pageable pageable) {
    log.info("Fetching posts for user: {}", userId);
    return postRepository.findByUserId(userId, pageable)
      .map(postMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PostResponse> getPostsByCategory(String categoryId, Pageable pageable) {
    log.info("Fetching posts for category: {}", categoryId);
    return postRepository.findByCategoryAndStatus(categoryId, PostStatus.ACTIVE.toString(), pageable)
      .map(postMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<PostResponse> searchPostsByTitle(String title, Pageable pageable) {
    log.info("Searching posts by title: {}", title);
    return postRepository.findByTitleContainingIgnoreCase(title, pageable)
      .map(postMapper::toResponse);
  }

  @Override
  @Transactional
  public PostResponse updatePost(String id, UpdatePostRequest request, String userId) {
    log.info("Updating post: {} for user: {}", id, userId);

    Post post = postRepository.findById(id)
      .orElseThrow(() -> {
        log.error("Post not found: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });

    // Verify ownership
    if (!post.getUser().getId().equals(userId)) {
      log.warn("User {} attempted to update post {} owned by {}", userId, id, post.getUser().getId());
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    // Update fields
    if (request.getTitle() != null) {
      post.setTitle(request.getTitle());
    }
    if (request.getPrice() != null) {
      post.setPrice(new BigDecimal(request.getPrice()));
    }
    if (request.getStatus() != null) {
      post.setStatus(PostStatus.valueOf(request.getStatus().toUpperCase()));
    }
    if (request.getCategoryId() != null) {
      Category category = categoryRepository.findById(request.getCategoryId())
        .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
      post.setCategory(category);
    }

    post = postRepository.save(post);
    log.info("Post updated: {}", id);
    return postMapper.toResponse(post);
  }

  @Override
  @Transactional
  public void deletePost(String id, String userId) {
    log.info("Deleting post: {} for user: {}", id, userId);

    Post post = postRepository.findById(id)
      .orElseThrow(() -> {
        log.error("Post not found: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });

    // Verify ownership
    if (!post.getUser().getId().equals(userId)) {
      log.warn("User {} attempted to delete post {} owned by {}", userId, id, post.getUser().getId());
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    postRepository.delete(post);
    log.info("Post deleted: {}", id);
  }
}

