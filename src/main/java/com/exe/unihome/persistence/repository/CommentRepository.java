package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.postAndComment.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, String> {

  Page<Comment> findByPostIdAndReplyIdIsNull(String postId, Pageable pageable);

  Page<Comment> findByReplyId(String replyId, Pageable pageable);

  Page<Comment> findByUserId(String userId, Pageable pageable);
}

