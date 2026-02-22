package com.exe.unihome.mapper;

import com.exe.unihome.dto.order.response.OrderItemResponse;
import com.exe.unihome.dto.sku.response.SkuAttributeValueResponse;
import com.exe.unihome.persistence.entity.FurnitureImage;
import com.exe.unihome.persistence.entity.FurnitureSku;
import com.exe.unihome.persistence.entity.order.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(source = "furniture.furnitureId", target = "furnitureId")
    @Mapping(source = "furniture.name", target = "furnitureName")
    @Mapping(source = "price", target = "unitPrice")
    @Mapping(target = "skuId", expression = "java(orderItem.getSku() != null ? orderItem.getSku().getSkuId() : null)")
    @Mapping(target = "skuCode", expression = "java(orderItem.getSku() != null ? orderItem.getSku().getSkuCode() : null)")
    @Mapping(target = "lineTotal", expression = "java(calculateLineTotal(orderItem))")
    @Mapping(target = "primaryImageUrl", expression = "java(resolvePrimaryImage(orderItem))")
    @Mapping(target = "skuAttributes", expression = "java(resolveSkuAttributes(orderItem))")
    OrderItemResponse toResponse(OrderItem orderItem);

    default BigDecimal calculateLineTotal(OrderItem orderItem) {
        if (orderItem.getPrice() == null || orderItem.getQuantity() == null) {
            return BigDecimal.ZERO;
        }
        return orderItem.getPrice().multiply(BigDecimal.valueOf(orderItem.getQuantity()));
    }

    default String resolvePrimaryImage(OrderItem orderItem) {
        // Prefer SKU image, fall back to furniture images
        FurnitureSku sku = orderItem.getSku();
        if (sku != null && sku.getImageUrl() != null) {
            return sku.getImageUrl();
        }
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

    default List<SkuAttributeValueResponse> resolveSkuAttributes(OrderItem orderItem) {
        FurnitureSku sku = orderItem.getSku();
        if (sku == null || sku.getAttributeValues() == null) {
            return Collections.emptyList();
        }
        return sku.getAttributeValues().stream()
                .map(av -> SkuAttributeValueResponse.builder()
                        .attributeTypeId(av.getAttributeType() != null ? av.getAttributeType().getAttributeTypeId() : null)
                        .attributeName(av.getAttributeType() != null ? av.getAttributeType().getName() : null)
                        .value(av.getValue())
                        .build())
                .toList();
    }
}
