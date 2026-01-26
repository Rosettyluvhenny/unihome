package com.exe.unihome.service.postAndSubscription;

import com.exe.unihome.dto.postAndComment.request.CreateCommentRequest;
import com.exe.unihome.dto.postAndComment.request.UpdateCommentRequest;
import com.exe.unihome.dto.postAndComment.response.CommentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentService {

  CommentResponse createComment(String postId, CreateCommentRequest request, String userId);

  CommentResponse getCommentById(String id);

  Page<CommentResponse> getCommentsByPostId(String postId, Pageable pageable);

  Page<CommentResponse> getCommentsByUserId(String userId, Pageable pageable);

  CommentResponse updateComment(String id, UpdateCommentRequest request, String userId);

  void deleteComment(String id, String userId);
}

