package com.exe.unihome.service;

import com.exe.unihome.dto.review.request.SubmitReviewRequest;
import com.exe.unihome.dto.review.response.FurnitureReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ReviewService {

    FurnitureReviewResponse submitReview(String userId, UUID furnitureId, SubmitReviewRequest request);

    Page<FurnitureReviewResponse> getReviewsForFurniture(UUID furnitureId, Pageable pageable);

    void deleteReview(String userId, UUID furnitureId);
}
