package com.exe.unihome.controller;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.dto.category.request.CreateCategoryRequest;
import com.exe.unihome.dto.category.request.UpdateCategoryRequest;
import com.exe.unihome.dto.category.response.CategoryResponse;
import com.exe.unihome.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
@Tag(name = "Category", description = "Category management APIs")
public class CategoryController {
    
    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Create a new category")
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<CategoryResponse>builder()
                        .code(0)
                        .message("Category created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get category by ID")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(
            @PathVariable UUID id) {
        CategoryResponse response = categoryService.getCategoryById(id);
        return ResponseEntity.ok(ApiResponse.<CategoryResponse>builder()
                .code(0)
                .message("Success")
                .data(response)
                .build());
    }

    @GetMapping
    @Operation(summary = "Get all categories with pagination")
    public ResponseEntity<ApiResponse<Page<CategoryResponse>>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CategoryResponse> response = categoryService.getAllCategories(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<CategoryResponse>>builder()
                .code(0)
                .message("Success")
                .data(response)
                .build());
    }

    @GetMapping("/all")
    @Operation(summary = "Get all categories without pagination")
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategoriesNoPaging() {
        List<CategoryResponse> response = categoryService.getAllCategories();
        return ResponseEntity.ok(ApiResponse.<List<CategoryResponse>>builder()
                .code(0)
                .message("Success")
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Update category")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.<CategoryResponse>builder()
                .code(0)
                .message("Category updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete category")
    public ResponseEntity<ApiResponse<Void>> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(0)
                .message("Category deleted successfully")
                .build());
    }
}
