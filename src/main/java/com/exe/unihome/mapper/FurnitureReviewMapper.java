package com.exe.unihome.mapper;

import com.exe.unihome.dto.review.response.FurnitureReviewResponse;
import com.exe.unihome.persistence.entity.review.FurnitureReview;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FurnitureReviewMapper {

    @Mapping(source = "furniture.furnitureId", target = "furnitureId")
    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "user.fullName", target = "userFullName")
    FurnitureReviewResponse toResponse(FurnitureReview review);
}
