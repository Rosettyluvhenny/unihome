package com.exe.unihome.mapper;

import com.exe.unihome.dto.order.response.OrderItemResponse;
import com.exe.unihome.persistence.entity.FurnitureImage;
import com.exe.unihome.persistence.entity.order.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.Comparator;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "furniture.furnitureId", target = "furnitureId")
    @Mapping(source = "furniture.name", target = "furnitureName")
    @Mapping(source = "price", target = "unitPrice")
    @Mapping(target = "lineTotal", expression = "java(calculateLineTotal(orderItem))")
    @Mapping(target = "primaryImageUrl", expression = "java(resolvePrimaryImage(orderItem))")
    OrderItemResponse toResponse(OrderItem orderItem);

    default BigDecimal calculateLineTotal(OrderItem orderItem) {
        if (orderItem.getPrice() == null || orderItem.getQuantity() == null) {
            return BigDecimal.ZERO;
        }
        return orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
    }

    default String resolvePrimaryImage(OrderItem orderItem) {
        if (orderItem.getFurniture() == null || orderItem.getFurniture().getImages() == null) {
            return null;
        }
        return orderItem.getFurniture().getImages().stream()
            .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()))
            .map(FurnitureImage::getImageUrl)
            .findFirst()
            .orElseGet(() -> orderItem.getFurniture().getImages().stream()
                .sorted(Comparator.comparing(FurnitureImage::getDisplayOrder)
                    .thenComparing(FurnitureImage::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(FurnitureImage::getImageUrl)
                .findFirst()
                .orElse(null));
    }
}
