package com.exe.unihome.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.config.WarehouseProperties;
import com.exe.unihome.dto.distance.DistanceResponse;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import com.exe.unihome.persistence.repository.UserRepository;
import com.exe.unihome.service.DistanceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DistanceServiceImpl implements DistanceService {

    private static final double EARTH_RADIUS_KM = 6371.0088d;

    private final UserRepository userRepository;
    private final WarehouseProperties warehouseProperties;

    @Override
    @Transactional(readOnly = true)
    public DistanceResponse getDistanceToWarehouse(String userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        BigDecimal userLatitude = user.getLatitude();
        BigDecimal userLongitude = user.getLongitude();

        if (userLatitude == null || userLongitude == null) {
            throw new AppException(ErrorCode.USER_LOCATION_NOT_SET);
        }

        Double warehouseLat = warehouseProperties.getLatitude();
        Double warehouseLon = warehouseProperties.getLongitude();

        if (warehouseLat == null || warehouseLon == null) {
            throw new AppException(ErrorCode.WAREHOUSE_LOCATION_NOT_CONFIGURED);
        }

        double userLatValue = userLatitude.doubleValue();
        double userLonValue = userLongitude.doubleValue();

        double distanceKm = haversine(userLatValue, userLonValue, warehouseLat, warehouseLon);
        double distanceMeters = distanceKm * 1000d;

        return DistanceResponse.builder()
            .userId(user.getId())
            .userAddress(user.getAddress())
            .userLatitude(userLatValue)
            .userLongitude(userLonValue)
            .warehouseAddress(warehouseProperties.getAddress())
            .warehouseLatitude(warehouseLat)
            .warehouseLongitude(warehouseLon)
            .distanceKilometers(distanceKm)
            .distanceMeters(distanceMeters)
            .formatted(formatDistance(distanceKm))
            .build();
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.pow(Math.sin(dLat / 2), 2)
            + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
            * Math.pow(Math.sin(dLon / 2), 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }

    private String formatDistance(double distanceKm) {
        return String.format("≈%.2f km", distanceKm);
    }
}
