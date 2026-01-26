package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.postAndComment.PostUserBoost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostUserBoostRepository extends JpaRepository<PostUserBoost, String> {

  List<PostUserBoost> findByPostId(String postId);

  List<PostUserBoost> findByUserBoostId(String userBoostId);

  Page<PostUserBoost> findByStatus(String status, Pageable pageable);
}

