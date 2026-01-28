package com.exe.unihome.postAndSubscription.controller;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.dto.postAndComment.request.CreatePostUserBoostRequest;
import com.exe.unihome.dto.postAndComment.request.UpdatePostUserBoostRequest;
import com.exe.unihome.dto.postAndComment.response.PostUserBoostResponse;
import com.exe.unihome.postAndSubscription.PostUserBoostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/boost-usage")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Post User Boost", description = "Post user boost management APIs")
public class PostUserBoostController {

  private final PostUserBoostService postUserBoostService;

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')|| hasRole('CUSTOMER')")
  @Operation(summary = "Create a new post user boost")
  public ResponseEntity<ApiResponse<PostUserBoostResponse>> createPostUserBoost(
    @Valid @RequestBody CreatePostUserBoostRequest request) {
    log.info("Creating post user boost for post: {}", request.getPostId());
    PostUserBoostResponse response = postUserBoostService.createPostUserBoost(request);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.<PostUserBoostResponse>builder()
        .code(0)
        .message("Post user boost created successfully")
        .data(response)
        .build());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get post user boost by ID")
  public ResponseEntity<ApiResponse<PostUserBoostResponse>> getPostUserBoostById(@PathVariable String id) {
    log.info("Fetching post user boost: {}", id);
    PostUserBoostResponse response = postUserBoostService.getPostUserBoostById(id);
    return ResponseEntity.ok(ApiResponse.<PostUserBoostResponse>builder()
      .code(0)
      .message("Post user boost retrieved successfully")
      .data(response)
      .build());
  }

  @GetMapping("/post/{postId}")
  @Operation(summary = "Get post user boosts by post ID")
  public ResponseEntity<ApiResponse<List<PostUserBoostResponse>>> getPostUserBoostsByPostId(
    @PathVariable String postId) {
    log.info("Fetching post user boosts for post: {}", postId);
    List<PostUserBoostResponse> responses = postUserBoostService.getPostUserBoostsByPostId(postId);
    return ResponseEntity.ok(ApiResponse.<List<PostUserBoostResponse>>builder()
      .code(0)
      .message("Post user boosts retrieved successfully")
      .data(responses)
      .build());
  }

  @GetMapping("/user-boost/{userBoostId}")
  @Operation(summary = "Get post user boosts by user boost ID")
  public ResponseEntity<ApiResponse<List<PostUserBoostResponse>>> getPostUserBoostsByUserBoostId(
    @PathVariable String userBoostId,
    @ParameterObject
    @PageableDefault(
      size = 10,
      page = 0,
      sort = "createdAt,desc", direction = Sort.Direction.DESC
    )
    Pageable pageable) {
    log.info("Fetching post user boosts for user boost: {}", userBoostId);
    List<PostUserBoostResponse> responses = postUserBoostService.getPostUserBoostsByUserBoostId(userBoostId, pageable);
    return ResponseEntity.ok(ApiResponse.<List<PostUserBoostResponse>>builder()
      .code(0)
      .message("Post user boosts retrieved successfully")
      .data(responses)
      .build());
  }

  @GetMapping
  @Operation(summary = "Get post user boosts by status")
  public ResponseEntity<ApiResponse<Page<PostUserBoostResponse>>> getPostUserBoostsByStatus(
    @RequestParam String status,
    @ParameterObject
    @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC
    ) Pageable pageable) {
    log.info("Fetching post user boosts with status: {}", status);
    Page<PostUserBoostResponse> responses = postUserBoostService.getPostUserBoostsByStatus(status, pageable);
    return ResponseEntity.ok(ApiResponse.<Page<PostUserBoostResponse>>builder()
      .code(0)
      .message("Post user boosts retrieved successfully")
      .data(responses)
      .build());
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')|| hasRole('CUSTOMER')")
  @Operation(summary = "Update post user boost")
  public ResponseEntity<ApiResponse<PostUserBoostResponse>> updatePostUserBoost(
    @PathVariable String id,
    @Valid @RequestBody UpdatePostUserBoostRequest request) {
    log.info("Updating post user boost: {}", id);
    PostUserBoostResponse response = postUserBoostService.updatePostUserBoost(id, request);
    return ResponseEntity.ok(ApiResponse.<PostUserBoostResponse>builder()
      .code(0)
      .message("Post user boost updated successfully")
      .data(response)
      .build());
  }

  @PostMapping("/{id}/cancel")
  @PreAuthorize("hasRole('ADMIN')||hasRole('CUSTOMER')")
  @Operation(summary = "Delete post user boost")
  public ResponseEntity<ApiResponse<PostUserBoostResponse>> cancelPostUserBoost(@PathVariable String id) {
    log.info("Deleting post user boost: {}", id);
    PostUserBoostResponse response = postUserBoostService.cancelPostUserBoost(id);
    return ResponseEntity.ok(ApiResponse.<PostUserBoostResponse>builder()
      .code(0)
      .message("Post user boost deleted successfully")
      .data(response)
      .build());
  }

}

