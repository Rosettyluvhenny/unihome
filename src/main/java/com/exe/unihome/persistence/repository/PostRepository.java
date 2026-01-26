package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.postAndComment.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, String> {

  Page<Post> findByUserId(String userId, Pageable pageable);

  List<Post> findByStatus(String status);

  Page<Post> findByTitleContainingIgnoreCase(String title, Pageable pageable);

  @Query("SELECT p FROM Post p WHERE p.category.categoryId = :categoryId AND p.status = :status")
  Page<Post> findByCategoryAndStatus(@Param("categoryId") String categoryId, @Param("status") String status, Pageable pageable);

  @Query("SELECT p FROM Post p LEFT JOIN FETCH p.postDetail LEFT JOIN FETCH p.comments WHERE p.id = :id")
  Optional<Post> findByIdWithDetails(@Param("id") String id);
}

