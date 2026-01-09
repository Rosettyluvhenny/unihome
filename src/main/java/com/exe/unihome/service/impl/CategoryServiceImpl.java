package com.exe.unihome.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.category.request.CreateCategoryRequest;
import com.exe.unihome.dto.category.request.UpdateCategoryRequest;
import com.exe.unihome.dto.category.response.CategoryResponse;
import com.exe.unihome.mapper.CategoryMapper;
import com.exe.unihome.persistence.entity.Category;
import com.exe.unihome.persistence.repository.CategoryRepository;
import com.exe.unihome.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        log.info("Creating category: {}", request.getName());
        
        // Check if category name already exists
        if (categoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        Category category = Category.builder()
                .name(request.getName())
                .build();
        
        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getCategoryId());
        
        return categoryMapper.toResponse(savedCategory);
    }

    @Override
    public CategoryResponse getCategoryById(UUID id) {
        log.info("Getting category by ID: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
        
        return categoryMapper.toResponse(category);
    }

    @Override
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        log.info("Getting all categories with pagination");
        
        return categoryRepository.findAll(pageable)
                .map(categoryMapper::toResponse);
    }

    @Override
    public List<CategoryResponse> getAllCategories() {
        log.info("Getting all categories");
        
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CategoryResponse updateCategory(UUID id, UpdateCategoryRequest request) {
        log.info("Updating category ID: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
        
        // Check if new name already exists (excluding current category)
        if (!category.getName().equals(request.getName()) && 
            categoryRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        category.setName(request.getName());
        
        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully");
        
        return categoryMapper.toResponse(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(UUID id) {
        log.info("Deleting category ID: {}", id);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
        
        categoryRepository.delete(category);
        log.info("Category deleted successfully");
    }
}
