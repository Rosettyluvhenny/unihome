package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.subscription.UserBoost;
import com.exe.unihome.persistence.entity.subscription.UserBoostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserBoostRepository extends JpaRepository<UserBoost, String> {

  List<UserBoost> findByUserId(String userId);

  Page<UserBoost> findByStatus(String status, Pageable pageable);

  Page<UserBoost> findByUserId(String userId, Pageable pageable);

  boolean existsByUserIdAndStatus(String userId, UserBoostStatus status);
}

