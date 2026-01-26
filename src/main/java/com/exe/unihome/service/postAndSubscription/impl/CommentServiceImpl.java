package com.exe.unihome.service.postAndSubscription.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.postAndComment.request.CreateCommentRequest;
import com.exe.unihome.dto.postAndComment.request.UpdateCommentRequest;
import com.exe.unihome.dto.postAndComment.response.CommentResponse;
import com.exe.unihome.mapper.CommentMapper;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import com.exe.unihome.persistence.entity.postAndComment.Comment;
import com.exe.unihome.persistence.entity.postAndComment.Post;
import com.exe.unihome.persistence.repository.CommentRepository;
import com.exe.unihome.persistence.repository.PostRepository;
import com.exe.unihome.persistence.repository.UserRepository;
import com.exe.unihome.service.postAndSubscription.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final CommentMapper commentMapper;

  @Override
  @Transactional
  public CommentResponse createComment(String postId, CreateCommentRequest request, String userId) {
    log.info("Creating comment for post: {} by user: {}", postId, userId);

    // Validate post exists
    Post post = postRepository.findById(postId)
      .orElseThrow(() -> {
        log.error("Post not found: {}", postId);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });

    // Validate user exists
    User user = userRepository.findById(userId)
      .orElseThrow(() -> {
        log.error("User not found: {}", userId);
        return new AppException(ErrorCode.USER_NOT_FOUND);
      });

    // If reply_id is provided, validate that comment exists
    Comment replyTo = null;
    if (request.getReplyId() != null && !request.getReplyId().isEmpty()) {
      replyTo = commentRepository.findById(request.getReplyId())
        .orElseThrow(() -> {
          log.error("Reply comment not found: {}", request.getReplyId());
          return new AppException(ErrorCode.INVALID_REQUEST);
        });
    }

    Comment comment = Comment.builder()
      .content(request.getContent())
      .post(post)
      .user(user)
      .reply(replyTo)
      .build();

    comment = commentRepository.save(comment);
    log.info("Comment created with ID: {}", comment.getId());
    return commentMapper.toResponse(comment);
  }

  @Override
  @Transactional(readOnly = true)
  public CommentResponse getCommentById(String id) {
    log.info("Fetching comment by ID: {}", id);
    Comment comment = commentRepository.findById(id)
      .orElseThrow(() -> {
        log.error("Comment not found: {}", id);
        return new AppException(ErrorCode.COMMENT_NOT_FOUND);
      });
    return commentMapper.toResponse(comment);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<CommentResponse> getCommentsByPostId(String postId, Pageable pageable) {
    log.info("Fetching comments for post: {}", postId);
    return commentRepository.findByPostIdAndReplyIdIsNull(postId, pageable)
      .map(commentMapper::toResponse);
  }

  @Override
  @Transactional(readOnly = true)
  public Page<CommentResponse> getCommentsByUserId(String userId, Pageable pageable) {
    log.info("Fetching comments for user: {}", userId);
    return commentRepository.findByUserId(userId, pageable)
      .map(commentMapper::toResponse);
  }

  @Override
  @Transactional
  public CommentResponse updateComment(String id, UpdateCommentRequest request, String userId) {
    log.info("Updating comment: {} for user: {}", id, userId);

    Comment comment = commentRepository.findById(id)
      .orElseThrow(() -> {
        log.error("Comment not found: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });

    // Verify ownership
    if (!comment.getUser().getId().equals(userId)) {
      log.warn("User {} attempted to update comment {} owned by {}", userId, id, comment.getUser().getId());
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    comment.setContent(request.getContent());
    comment = commentRepository.save(comment);
    log.info("Comment updated: {}", id);
    return commentMapper.toResponse(comment);
  }

  @Override
  @Transactional
  public void deleteComment(String id, String userId) {
    log.info("Deleting comment: {} for user: {}", id, userId);

    Comment comment = commentRepository.findById(id)
      .orElseThrow(() -> {
        log.error("Comment not found: {}", id);
        return new AppException(ErrorCode.INVALID_REQUEST);
      });

    // Verify ownership
    if (!comment.getUser().getId().equals(userId)) {
      log.warn("User {} attempted to delete comment {} owned by {}", userId, id, comment.getUser().getId());
      throw new AppException(ErrorCode.UNAUTHORIZED);
    }

    commentRepository.delete(comment);
    log.info("Comment deleted: {}", id);
  }

  @Override
  public Page<CommentResponse> getCommentByReplyId(String replyId, Pageable pageable) {
    return commentRepository.findByReplyId(replyId, pageable)
      .map(commentMapper::toResponse);
  }
}

