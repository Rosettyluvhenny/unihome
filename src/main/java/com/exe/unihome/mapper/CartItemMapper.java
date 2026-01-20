package com.exe.unihome.mapper;

import com.exe.unihome.dto.cart.response.CartItemResponse;
import com.exe.unihome.persistence.entity.FurnitureImage;
import com.exe.unihome.persistence.entity.cart.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(source = "furniture.furnitureId", target = "furnitureId")
    @Mapping(source = "furniture.name", target = "furnitureName")
    @Mapping(target = "primaryImageUrl", expression = "java(resolvePrimaryImage(cartItem))")
    CartItemResponse toResponse(CartItem cartItem);

    default String resolvePrimaryImage(CartItem cartItem) {
        if (cartItem.getFurniture() == null || cartItem.getFurniture().getImages() == null) {
            return null;
        }
        return cartItem.getFurniture().getImages().stream()
                .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()))
                .map(FurnitureImage::getImageUrl)
                .findFirst()
                .orElseGet(() -> cartItem.getFurniture().getImages().stream()
                        .sorted(Comparator.comparing(FurnitureImage::getDisplayOrder)
                                .thenComparing(FurnitureImage::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(FurnitureImage::getImageUrl)
                        .findFirst()
                        .orElse(null));
    }
}
