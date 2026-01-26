package com.exe.unihome.service.postAndSubscription;

import com.exe.unihome.dto.subscription.request.CreateUserBoostRequest;
import com.exe.unihome.dto.subscription.response.UserBoostResponse;
import com.exe.unihome.persistence.entity.subscription.UserBoostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserBoostService {

  UserBoostResponse createUserBoost(CreateUserBoostRequest request);

  UserBoostResponse getUserBoostById(String id);

  List<UserBoostResponse> getUserBoostsByUserId(String userId);

  Page<UserBoostResponse> getUserBoostsByUserId(String userId, Pageable pageable);

  Page<UserBoostResponse> getUserBoostsByStatus(String status, Pageable pageable);

  Page<UserBoostResponse> getAllUserBoosts(Pageable pageable);

  UserBoostResponse updateUserBoostStatus(String id, UserBoostStatus userBoostStatus);

  void deleteUserBoost(String id, String userId, boolean isAdmin);
}

