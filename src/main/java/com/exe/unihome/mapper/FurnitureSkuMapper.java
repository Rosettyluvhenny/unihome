package com.exe.unihome.mapper;

import com.exe.unihome.dto.sku.response.AttributeTypeResponse;
import com.exe.unihome.dto.sku.response.SkuAttributeValueResponse;
import com.exe.unihome.dto.sku.response.SkuResponse;
import com.exe.unihome.persistence.entity.FurnitureAttributeType;
import com.exe.unihome.persistence.entity.FurnitureSku;
import com.exe.unihome.persistence.entity.SkuAttributeValue;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FurnitureSkuMapper {

    @Mapping(source = "status", target = "status")
    @Mapping(target = "attributes", source = "attributeValues")
    SkuResponse toResponse(FurnitureSku sku);

    @Mapping(source = "attributeType.attributeTypeId", target = "attributeTypeId")
    @Mapping(source = "attributeType.name", target = "attributeName")
    SkuAttributeValueResponse toAttributeValueResponse(SkuAttributeValue attrValue);

    AttributeTypeResponse toAttributeTypeResponse(FurnitureAttributeType attributeType);

    List<AttributeTypeResponse> toAttributeTypeResponseList(List<FurnitureAttributeType> attributeTypes);
}
