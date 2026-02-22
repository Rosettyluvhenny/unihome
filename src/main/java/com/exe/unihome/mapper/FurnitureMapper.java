package com.exe.unihome.mapper;

import com.exe.unihome.dto.furniture.response.FurnitureResponse;
import com.exe.unihome.persistence.entity.Furniture;
import com.exe.unihome.persistence.entity.FurnitureImage;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring", uses = {FurnitureImageMapper.class, FurnitureSkuMapper.class})
public interface FurnitureMapper {

    @Mapping(source = "category.categoryId", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "status", target = "status")
    @Mapping(source = "images", target = "images")
    @Mapping(target = "primaryImageUrl", ignore = true)
    @Mapping(source = "attributeTypes", target = "attributeTypes")
    @Mapping(source = "skus", target = "skus")
    FurnitureResponse toResponse(Furniture furniture);

    @AfterMapping
    default void enrichResponse(@MappingTarget FurnitureResponse response, Furniture furniture) {
        // Resolve furniture primary image
        List<FurnitureImage> images = furniture.getImages();
        if (images != null && !images.isEmpty()) {
            String primary = images.stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                    .map(FurnitureImage::getImageUrl)
                    .findFirst()
                    .orElseGet(() -> images.stream()
                            .sorted(Comparator.comparing(FurnitureImage::getDisplayOrder)
                                    .thenComparing(FurnitureImage::getCreatedAt))
                            .map(FurnitureImage::getImageUrl)
                            .findFirst()
                            .orElse(null));
            response.setPrimaryImageUrl(primary);
        }
        // SKU primaryImageUrl is handled by FurnitureSkuMapper's @AfterMapping
    }
}
