package com.exe.unihome.controller;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.dto.review.request.SubmitReviewRequest;
import com.exe.unihome.dto.review.response.FurnitureReviewResponse;
import com.exe.unihome.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.DeleteMapping;

import java.util.UUID;

@RestController
@RequestMapping("/furniture/{furnitureId}/reviews")
@RequiredArgsConstructor
@Tag(name = "Reviews", description = "Furniture rating and review APIs")
@SecurityRequirement(name = "bearerAuth")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Create or update a review for furniture")
    public ResponseEntity<ApiResponse<FurnitureReviewResponse>> submitReview(
            Authentication authentication,
            @PathVariable UUID furnitureId,
            @Valid @RequestBody SubmitReviewRequest request) {
        FurnitureReviewResponse response = reviewService.submitReview(authentication.getName(), furnitureId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.<FurnitureReviewResponse>builder()
                .code(0)
                .message("Review saved")
                .data(response)
                .build());
    }

    @GetMapping
    @Operation(summary = "List reviews for a furniture item")
    public ResponseEntity<ApiResponse<Page<FurnitureReviewResponse>>> getReviews(
            @PathVariable UUID furnitureId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<FurnitureReviewResponse> response = reviewService.getReviewsForFurniture(furnitureId, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<FurnitureReviewResponse>>builder()
            .code(0)
            .message("Success")
            .data(response)
            .build());
    }

    @DeleteMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Delete the authenticated user's review for furniture")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            Authentication authentication,
            @PathVariable UUID furnitureId) {
        reviewService.deleteReview(authentication.getName(), furnitureId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
            .code(0)
            .message("Review deleted")
            .build());
    }
}
