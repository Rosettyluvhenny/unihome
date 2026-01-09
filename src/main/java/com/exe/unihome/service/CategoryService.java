package com.exe.unihome.service;

import com.exe.unihome.dto.category.request.CreateCategoryRequest;
import com.exe.unihome.dto.category.request.UpdateCategoryRequest;
import com.exe.unihome.dto.category.response.CategoryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    
    CategoryResponse createCategory(CreateCategoryRequest request);
    
    CategoryResponse getCategoryById(UUID id);
    
    Page<CategoryResponse> getAllCategories(Pageable pageable);
    
    List<CategoryResponse> getAllCategories();
    
    CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request);
    
    void deleteCategory(UUID id);
}
