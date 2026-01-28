package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.postAndComment.PostUserBoost;
import com.exe.unihome.persistence.entity.postAndComment.PostUserBoostStatus;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface PostUserBoostRepository extends JpaRepository<PostUserBoost, String> {

  List<PostUserBoost> findByPostId(String postId);

  Page<PostUserBoost> findByUserBoostId(String userBoostId, Pageable pageable);

  Page<PostUserBoost> findByStatus(String status, Pageable pageable);

//  List<PostUserBoost> findAllByStatusAndEndTimeBefore(PostUserBoostStatus status, LocalDateTime now);
//
//  List<PostUserBoost> findAllByStatusAndStartTimeBefore(PostUserBoostStatus postUserBoostStatus, LocalDateTime now);

  List<PostUserBoost> findAllByPostIdAndStatus(@NotNull(message = "Post ID is required") String postId, PostUserBoostStatus postUserBoostStatus);

  @Modifying
  @Query("""
    UPDATE PostUserBoost p
    SET p.status = 'ACTIVE'
    WHERE p.status = 'SCHEDULE'
      AND p.startTime <= :now
    """)
  int markOnGoing(@Param("now") LocalDateTime now);

  @Modifying
  @Query("""
    UPDATE PostUserBoost p
    SET p.status = 'FINISH'
    WHERE p.status = 'ACTIVE'
      AND p.endTime <= :now
    """)
  int markFinished(@Param("now") LocalDateTime now);
}

