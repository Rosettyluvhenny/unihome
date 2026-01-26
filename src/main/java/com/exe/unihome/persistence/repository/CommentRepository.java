package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.postAndComment.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, String> {

  Page<Comment> findByPostId(String postId, Pageable pageable);

  List<Comment> findByReplyId(String replyId);

  Page<Comment> findByUserId(String userId, Pageable pageable);
}

