package com.exe.unihome.mapper;

import com.exe.unihome.dto.category.response.CategoryResponse;
import com.exe.unihome.persistence.entity.Category;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    
    CategoryResponse toResponse(Category category);
}
