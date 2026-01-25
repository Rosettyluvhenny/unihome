package com.exe.unihome.dto.distance;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DistanceResponse {

    private String userId;
    private String userAddress;
    private Double userLatitude;
    private Double userLongitude;

    private String warehouseAddress;
    private Double warehouseLatitude;
    private Double warehouseLongitude;

    private Double distanceMeters;
    private Double distanceKilometers;
    private String formatted;
}
