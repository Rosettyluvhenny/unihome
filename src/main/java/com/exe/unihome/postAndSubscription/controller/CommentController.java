package com.exe.unihome.postAndSubscription.controller;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.dto.postAndComment.request.CreateCommentRequest;
import com.exe.unihome.dto.postAndComment.request.UpdateCommentRequest;
import com.exe.unihome.dto.postAndComment.response.CommentResponse;
import com.exe.unihome.postAndSubscription.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Comment", description = "Comment management APIs")
@SecurityRequirement(name = "bearerAuth")
public class CommentController {

  private final CommentService commentService;

  @PostMapping("/post/{postId}")
  @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
  @Operation(summary = "Create a new comment on a post")
  public ResponseEntity<ApiResponse<CommentResponse>> createComment(
    @PathVariable String postId,
    @Valid @RequestBody CreateCommentRequest request,
    Authentication authentication) {
    log.info("Creating comment on post: {} by user: {}", postId, authentication.getName());
    CommentResponse response = commentService.createComment(postId, request, authentication.getName());
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.<CommentResponse>builder()
        .code(0)
        .message("Comment created successfully")
        .data(response)
        .build());
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get comment by ID")
  public ResponseEntity<ApiResponse<CommentResponse>> getCommentById(@PathVariable String id) {
    log.info("Fetching comment: {}", id);
    CommentResponse response = commentService.getCommentById(id);
    return ResponseEntity.ok(ApiResponse.<CommentResponse>builder()
      .code(0)
      .message("Comment retrieved successfully")
      .data(response)
      .build());
  }

  @GetMapping("/post/{postId}")
  @Operation(summary = "Get comments for a post")
  public ResponseEntity<ApiResponse<Page<CommentResponse>>> getCommentsByPostId(
    @PathVariable String postId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {
    log.info("Fetching comments for post: {}", postId);
    Pageable pageable = PageRequest.of(page, size);
    Page<CommentResponse> responses = commentService.getCommentsByPostId(postId, pageable);
    return ResponseEntity.ok(ApiResponse.<Page<CommentResponse>>builder()
      .code(0)
      .message("Comments retrieved successfully")
      .data(responses)
      .build());
  }

  @GetMapping("/reply/{id}")
  @Operation(summary = "Get comments for a post")
  public ResponseEntity<ApiResponse<Page<CommentResponse>>> getCommentsByReplyId(
    @PathVariable String id,
    @ParameterObject
    @PageableDefault(size = 10, page = 0, sort = "createdAt", direction = Sort.Direction.DESC
    ) Pageable pageable) {
    log.info("Fetching comments for post: {}", id);
    Page<CommentResponse> responses = commentService.getCommentByReplyId(id, pageable);
    return ResponseEntity.ok(ApiResponse.<Page<CommentResponse>>builder()
      .code(0)
      .message("Comments retrieved successfully")
      .data(responses)
      .build());
  }

  @GetMapping("/user/{userId}")
  @Operation(summary = "Get comments by user ID")
  public ResponseEntity<ApiResponse<Page<CommentResponse>>> getCommentsByUserId(
    @PathVariable String userId,
    @RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {
    log.info("Fetching comments for user: {}", userId);
    Pageable pageable = PageRequest.of(page, size);
    Page<CommentResponse> responses = commentService.getCommentsByUserId(userId, pageable);
    return ResponseEntity.ok(ApiResponse.<Page<CommentResponse>>builder()
      .code(0)
      .message("User comments retrieved successfully")
      .data(responses)
      .build());
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
  @Operation(summary = "Update comment")
  public ResponseEntity<ApiResponse<CommentResponse>> updateComment(
    @PathVariable String id,
    @Valid @RequestBody UpdateCommentRequest request,
    Authentication authentication) {
    log.info("Updating comment: {} by user: {}", id, authentication.getName());
    CommentResponse response = commentService.updateComment(id, request, authentication.getName());
    return ResponseEntity.ok(ApiResponse.<CommentResponse>builder()
      .code(0)
      .message("Comment updated successfully")
      .data(response)
      .build());
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('CUSTOMER') or hasRole('ADMIN')")
  @Operation(summary = "Delete comment")
  public ResponseEntity<ApiResponse<Void>> deleteComment(
    @PathVariable String id,
    Authentication authentication) {
    log.info("Deleting comment: {} by user: {}", id, authentication.getName());
    commentService.deleteComment(id, authentication.getName());
    return ResponseEntity.ok(ApiResponse.<Void>builder()
      .code(0)
      .message("Comment deleted successfully")
      .build());
  }

}

