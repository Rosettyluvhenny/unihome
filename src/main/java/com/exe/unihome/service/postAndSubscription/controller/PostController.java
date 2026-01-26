package com.exe.unihome.service.postAndSubscription.controller;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.dto.postAndComment.request.CreatePostRequest;
import com.exe.unihome.dto.postAndComment.request.UpdatePostRequest;
import com.exe.unihome.dto.postAndComment.response.PostResponse;
import com.exe.unihome.service.postAndSubscription.PostService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
public class PostController {

  private final PostService postService;

  @PostMapping
  @Operation(summary = "Create a new post")
  public ResponseEntity<ApiResponse<PostResponse>> createPost(
    @Valid @RequestBody CreatePostRequest request,
    Authentication authentication) {
    log.info("Creating post by user: {}", authentication.getName());
    PostResponse response = postService.createPost(request, authentication.getName());
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.<PostResponse>builder()
        .code(0)
        .message("Post created successfully")
        .data(response)
        .build());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get post by ID")
  public ResponseEntity<ApiResponse<PostResponse>> getPostById(@PathVariable String id) {
    log.info("Fetching post: {}", id);
    PostResponse response = postService.getPostById(id);
    return ResponseEntity.ok(ApiResponse.<PostResponse>builder()
      .code(0)
      .message("Post retrieved successfully")
      .data(response)
      .build());
  }

  @GetMapping
  @Operation(summary = "Get all posts with pagination")
  public ResponseEntity<ApiResponse<Page<PostResponse>>> getAllPosts(
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size,
    @RequestParam(defaultValue = "createdAt") String sortBy,
    @RequestParam(defaultValue = "DESC") String sortDirection) {
    log.info("Fetching all posts - page: {}, size: {}", page, size);
    Sort sort = Sort.by(Sort.Direction.valueOf(sortDirection), sortBy);
    Pageable pageable = PageRequest.of(page, size, sort);
    Page<PostResponse> responses = postService.getAllPosts(pageable);
    return ResponseEntity.ok(ApiResponse.<Page<PostResponse>>builder()
      .code(0)
      .message("Posts retrieved successfully")
      .data(responses)
      .build());
  }

  @GetMapping("/user/{userId}")
  @Operation(summary = "Get posts by user ID")
  public ResponseEntity<ApiResponse<Page<PostResponse>>> getPostsByUserId(
    @PathVariable String userId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {
    log.info("Fetching posts for user: {}", userId);
    Pageable pageable = PageRequest.of(page, size);
    Page<PostResponse> responses = postService.getPostsByUserId(userId, pageable);
    return ResponseEntity.ok(ApiResponse.<Page<PostResponse>>builder()
      .code(0)
      .message("User posts retrieved successfully")
      .data(responses)
      .build());
  }

  @GetMapping("/category/{categoryId}")
  @Operation(summary = "Get posts by category")
  public ResponseEntity<ApiResponse<Page<PostResponse>>> getPostsByCategory(
    @PathVariable String categoryId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {
    log.info("Fetching posts for category: {}", categoryId);
    Pageable pageable = PageRequest.of(page, size);
    Page<PostResponse> responses = postService.getPostsByCategory(categoryId, pageable);
    return ResponseEntity.ok(ApiResponse.<Page<PostResponse>>builder()
      .code(0)
      .message("Category posts retrieved successfully")
      .data(responses)
      .build());
  }

  @GetMapping("/search")
  @Operation(summary = "Search posts by title")
  public ResponseEntity<ApiResponse<Page<PostResponse>>> searchPosts(
    @RequestParam String title,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {
    log.info("Searching posts with title: {}", title);
    Pageable pageable = PageRequest.of(page, size);
    Page<PostResponse> responses = postService.searchPostsByTitle(title, pageable);
    return ResponseEntity.ok(ApiResponse.<Page<PostResponse>>builder()
      .code(0)
      .message("Posts found successfully")
      .data(responses)
      .build());
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  @Operation(summary = "Update post")
  public ResponseEntity<ApiResponse<PostResponse>> updatePost(
    @PathVariable String id,
    @Valid @RequestBody UpdatePostRequest request,
    Authentication authentication) {
    log.info("Updating post: {} by user: {}", id, authentication.getName());
    PostResponse response = postService.updatePost(id, request, authentication.getName());
    return ResponseEntity.ok(ApiResponse.<PostResponse>builder()
      .code(0)
      .message("Post updated successfully")
      .data(response)
      .build());
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
  @Operation(summary = "Delete post")
  public ResponseEntity<ApiResponse<Void>> deletePost(
    @PathVariable String id,
    Authentication authentication) {
    log.info("Deleting post: {} by user: {}", id, authentication.getName());
    postService.deletePost(id, authentication.getName());
    return ResponseEntity.ok(ApiResponse.<Void>builder()
      .code(0)
      .message("Post deleted successfully")
      .build());
  }
}

