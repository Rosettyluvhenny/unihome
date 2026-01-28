package com.exe.unihome.postAndSubscription.controller;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.dto.postAndComment.request.CreatePostRequest;
import com.exe.unihome.dto.postAndComment.request.UpdatePostRequest;
import com.exe.unihome.dto.postAndComment.response.PostResponse;
import com.exe.unihome.postAndSubscription.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Post", description = "Post management APIs")
public class PostController {

  private final PostService postService;

  @PostMapping
  @Operation(summary = "Create a new post")
  @PreAuthorize("hasRole('ADMIN')||hasRole('CUSTOMER')")
  public ResponseEntity<ApiResponse<PostResponse>> createPost(
    @Valid @RequestBody CreatePostRequest request,
    Authentication authentication) {
    PostResponse response = postService.createPost(request);
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
    @ParameterObject
    @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC
    ) Pageable pageable) {
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
    @ParameterObject
    @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC
    ) Pageable pageable) {
    log.info("Fetching posts for user: {}", userId);
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
    @PathVariable UUID categoryId,
    @ParameterObject
    @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC
    ) Pageable pageable) {
    log.info("Fetching posts for category: {}", categoryId);
    Page<PostResponse> responses = postService.getPostsByCategory(categoryId, pageable);
    return ResponseEntity.ok(ApiResponse.<Page<PostResponse>>builder()
      .code(0)
      .message("Category posts retrieved successfully")
      .data(responses)
      .build());
  }

  @GetMapping("/search")
  @Operation(summary = "Search posts by title")
  @Description("Search null  return all by order of Boost Endtime and created Order")
  public ResponseEntity<ApiResponse<Page<PostResponse>>> searchPosts(
    @RequestParam String title,
    @ParameterObject
    @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC
    ) Pageable pageable) {
    Page<PostResponse> responses = postService.searchPostsByTitle(title, pageable);
    return ResponseEntity.ok(ApiResponse.<Page<PostResponse>>builder()
      .code(0)
      .message("Posts found successfully")
      .data(responses)
      .build());
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
  @Operation(summary = "Update post")
  public ResponseEntity<ApiResponse<PostResponse>> updatePost(
    @PathVariable String id,
    @Valid @RequestBody UpdatePostRequest request) {
    PostResponse response = postService.updatePost(id, request);
    return ResponseEntity.ok(ApiResponse.<PostResponse>builder()
      .code(0)
      .message("Post updated successfully")
      .data(response)
      .build());
  }

  @PutMapping("/{id}/delete")
  @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
  @Operation(summary = "Delete post")
  public ResponseEntity<ApiResponse<Void>> deletePost(
    @PathVariable String id) {
    postService.disabledPost(id);
    return ResponseEntity.ok(ApiResponse.<Void>builder()
      .code(0)
      .message("Post deleted successfully")
      .build());
  }


}

