package com.exe.unihome.postAndSubscription;

import com.exe.unihome.dto.subscription.request.CreateBoostRequest;
import com.exe.unihome.dto.subscription.request.UpdateBoostRequest;
import com.exe.unihome.dto.subscription.response.BoostResponse;
import com.exe.unihome.persistence.entity.subscription.BoostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoostService {

  BoostResponse createBoost(CreateBoostRequest request);

  BoostResponse getBoostById(String id);

  Page<BoostResponse> getAllBoosts(Pageable pageable);

  Page<BoostResponse> getBoostsByStatus(BoostStatus status, Pageable pageable);

  BoostResponse updateBoost(String id, UpdateBoostRequest request);

  void deleteBoost(String id);
}

