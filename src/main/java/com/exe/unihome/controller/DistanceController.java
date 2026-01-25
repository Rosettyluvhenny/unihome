package com.exe.unihome.controller;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.dto.distance.DistanceResponse;
import com.exe.unihome.service.DistanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/distance")
@RequiredArgsConstructor
@Tag(name = "Distance", description = "Utility APIs for location distance calculations")
@SecurityRequirement(name = "bearerAuth")
public class DistanceController {

    private final DistanceService distanceService;

    @GetMapping("/warehouse")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get distance from current user to warehouse")
    public ResponseEntity<ApiResponse<DistanceResponse>> getDistanceToWarehouse(Authentication authentication) {
        DistanceResponse response = distanceService.getDistanceToWarehouse(authentication.getName());
        return ResponseEntity.ok(ApiResponse.<DistanceResponse>builder()
            .code(0)
            .message("Success")
            .data(response)
            .build());
    }
}
