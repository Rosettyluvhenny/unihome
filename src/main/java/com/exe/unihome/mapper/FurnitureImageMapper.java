package com.exe.unihome.mapper;

import com.exe.unihome.dto.furniture.response.FurnitureImageResponse;
import com.exe.unihome.persistence.entity.FurnitureImage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FurnitureImageMapper {
    FurnitureImageResponse toResponse(FurnitureImage image);
}
