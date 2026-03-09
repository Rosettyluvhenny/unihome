package com.exe.unihome.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.review.request.SubmitReviewRequest;
import com.exe.unihome.dto.review.response.FurnitureReviewResponse;
import com.exe.unihome.mapper.FurnitureReviewMapper;
import com.exe.unihome.persistence.entity.Furniture;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import com.exe.unihome.persistence.entity.review.FurnitureReview;
import com.exe.unihome.persistence.enums.OrderStatus;
import com.exe.unihome.persistence.repository.FurnitureRepository;
import com.exe.unihome.persistence.repository.FurnitureReviewRepository;
import com.exe.unihome.persistence.repository.OrderItemRepository;
import com.exe.unihome.persistence.repository.UserRepository;
import com.exe.unihome.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private static final Set<OrderStatus> QUALIFIED_STATUSES = EnumSet.of(OrderStatus.COMPLETED);

    private final FurnitureRepository furnitureRepository;
    private final UserRepository userRepository;
    private final FurnitureReviewRepository furnitureReviewRepository;
    private final OrderItemRepository orderItemRepository;
    private final FurnitureReviewMapper furnitureReviewMapper;

    @Override
    @Transactional
    public FurnitureReviewResponse submitReview(String userId, UUID furnitureId, SubmitReviewRequest request) {
        Furniture furniture = furnitureRepository.findById(furnitureId)
            .orElseThrow(() -> new AppException(ErrorCode.FURNITURE_NOT_FOUND));
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));

        if (!hasUserPurchased(userId, furnitureId)) {
            throw new AppException(ErrorCode.REVIEW_NOT_ALLOWED);
        }

        FurnitureReview review = furnitureReviewRepository
            .findByFurnitureFurnitureIdAndUser_Id(furnitureId, userId)
            .map(existing -> {
                existing.setRating(request.getRating().shortValue());
                existing.setComment(request.getComment());
                return existing;
            })
            .orElseGet(() -> FurnitureReview.builder()
                .furniture(furniture)
                .user(user)
                .rating(request.getRating().shortValue())
                .comment(request.getComment())
                .build());

        FurnitureReview saved = furnitureReviewRepository.save(review);
        return furnitureReviewMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FurnitureReviewResponse> getReviewsForFurniture(UUID furnitureId, Pageable pageable) {
        if (!furnitureRepository.existsById(furnitureId)) {
            throw new AppException(ErrorCode.FURNITURE_NOT_FOUND);
        }
        return furnitureReviewRepository.findByFurnitureFurnitureId(furnitureId, pageable)
            .map(furnitureReviewMapper::toResponse);
    }

    @Override
    @Transactional
    public void deleteReview(String userId, UUID furnitureId) {
        FurnitureReview review = furnitureReviewRepository
            .findByFurnitureFurnitureIdAndUser_Id(furnitureId, userId)
            .orElseThrow(() -> new AppException(ErrorCode.REVIEW_NOT_FOUND));
        furnitureReviewRepository.delete(review);
    }

    private boolean hasUserPurchased(String userId, UUID furnitureId) {
        return orderItemRepository.existsByOrder_User_IdAndFurniture_FurnitureIdAndOrder_StatusIn(
            userId,
            furnitureId,
            QUALIFIED_STATUSES
        );
    }
}
