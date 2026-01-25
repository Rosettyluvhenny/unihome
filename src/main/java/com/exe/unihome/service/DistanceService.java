package com.exe.unihome.service;

import com.exe.unihome.dto.distance.DistanceResponse;

public interface DistanceService {

    DistanceResponse getDistanceToWarehouse(String userId);
}
