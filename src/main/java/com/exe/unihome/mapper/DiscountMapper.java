package com.exe.unihome.mapper;

import com.exe.unihome.dto.discount.request.CreateDiscountRequest;
import com.exe.unihome.dto.discount.response.DiscountResponse;
import com.exe.unihome.persistence.entity.Discount;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DiscountMapper {
    
    @Mapping(target = "furnitureDiscounts", ignore = true)
    Discount toEntity(CreateDiscountRequest request);
    
    @Mapping(target = "isActive", ignore = true)
    @Mapping(target = "appliedFurnitureCount", ignore = true)
    DiscountResponse toResponse(Discount discount);
}

