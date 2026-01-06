package com.exe.unihome.mapper;

import com.exe.unihome.dto.furniture.response.FurnitureResponse;
import com.exe.unihome.entity.Furniture;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FurnitureMapper {
    
    @Mapping(source = "category.categoryId", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    @Mapping(source = "status", target = "status")
    FurnitureResponse toResponse(Furniture furniture);
}
