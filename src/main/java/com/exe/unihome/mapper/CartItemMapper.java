package com.exe.unihome.mapper;

import com.exe.unihome.dto.cart.response.CartItemResponse;
import com.exe.unihome.persistence.entity.FurnitureImage;
import com.exe.unihome.persistence.entity.FurnitureSku;
import com.exe.unihome.persistence.entity.cart.CartItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Comparator;

@Mapper(componentModel = "spring")
public interface CartItemMapper {

    @Mapping(source = "furniture.furnitureId", target = "furnitureId")
    @Mapping(source = "furniture.name", target = "furnitureName")
    @Mapping(target = "skuId", expression = "java(cartItem.getSku() != null ? cartItem.getSku().getSkuId() : null)")
    @Mapping(target = "skuCode", expression = "java(cartItem.getSku() != null ? cartItem.getSku().getSkuCode() : null)")
    @Mapping(target = "primaryImageUrl", expression = "java(resolvePrimaryImage(cartItem))")
    CartItemResponse toResponse(CartItem cartItem);

    default String resolvePrimaryImage(CartItem cartItem) {
        // Prefer SKU image, fall back to furniture images
        FurnitureSku sku = cartItem.getSku();
        if (sku != null && sku.getImageUrl() != null) {
            return sku.getImageUrl();
        }
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
