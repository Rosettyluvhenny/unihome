package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.postAndComment.Post;
import com.exe.unihome.persistence.entity.postAndComment.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, String> {

  Page<Post> findByUserIdAndStatus(String userId, PostStatus status, Pageable pageable);

  Optional<Post> findByIdAndStatus(String userId, PostStatus status);

  Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);

  @Query("SELECT p FROM Post p WHERE p.category.categoryId = :categoryId AND p.status = :status")
  Page<Post> findByCategoryIdAndStatus(@Param("categoryId") UUID categoryId, @Param("status") PostStatus status, Pageable pageable);

  @Query("SELECT p FROM Post p LEFT JOIN FETCH p.postDetail WHERE p.id = :id AND p.status = :status")
  Optional<Post> findByIdAndStatusWithDetails(@Param("id") String id, @Param("status") PostStatus status);


  Page<Post> findByTitleContainingIgnoreCaseAndStatus(String title, PostStatus status, Pageable pageable);

  Page<Post> findAllByStatus(PostStatus postStatus, Pageable pageable);
}

