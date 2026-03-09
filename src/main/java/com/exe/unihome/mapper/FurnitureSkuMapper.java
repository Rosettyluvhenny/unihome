package com.exe.unihome.mapper;

import com.exe.unihome.dto.sku.response.SkuResponse;
import com.exe.unihome.persistence.entity.FurnitureSku;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FurnitureSkuMapper {

    @Mapping(source = "status", target = "status")
    SkuResponse toResponse(FurnitureSku sku);
}
