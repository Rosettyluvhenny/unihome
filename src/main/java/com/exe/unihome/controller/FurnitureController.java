package com.exe.unihome.controller;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.dto.furniture.request.CreateFurnitureRequest;
import com.exe.unihome.dto.furniture.request.UpdateFurnitureRequest;
import com.exe.unihome.dto.furniture.response.FurnitureResponse;
import com.exe.unihome.service.FurnitureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
@RequestMapping("/furniture")
@RequiredArgsConstructor
@Tag(name = "Furniture", description = "Furniture management APIs")
@SecurityRequirement(name = "bearerAuth")
public class FurnitureController {
    
    private final FurnitureService furnitureService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Create a new furniture item")
    public ResponseEntity<ApiResponse<FurnitureResponse>> createFurniture(
            @Valid @RequestBody CreateFurnitureRequest request) {
        FurnitureResponse response = furnitureService.createFurniture(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<FurnitureResponse>builder()
                        .code(0)
                        .message("Furniture created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get furniture by ID")
    public ResponseEntity<ApiResponse<FurnitureResponse>> getFurnitureById(
            @PathVariable UUID id) {
        FurnitureResponse response = furnitureService.getFurnitureById(id);
        return ResponseEntity.ok(ApiResponse.<FurnitureResponse>builder()
                .code(0)
                .message("Success")
                .data(response)
                .build());
    }

    @GetMapping
    @Operation(summary = "Get all furniture with pagination")
    public ResponseEntity<ApiResponse<Page<FurnitureResponse>>> getAllFurniture(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("ASC") 
                ? Sort.by(sortBy).ascending() 
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<FurnitureResponse> response = furnitureService.getAllFurniture(pageable);
        return ResponseEntity.ok(ApiResponse.<Page<FurnitureResponse>>builder()
                .code(0)
                .message("Success")
                .data(response)
                .build());
    }

    @GetMapping("/category/{categoryId}")
    @Operation(summary = "Get furniture by category")
    public ResponseEntity<ApiResponse<List<FurnitureResponse>>> getFurnitureByCategory(
            @PathVariable UUID categoryId) {
        List<FurnitureResponse> response = furnitureService.getFurnitureByCategory(categoryId);
        return ResponseEntity.ok(ApiResponse.<List<FurnitureResponse>>builder()
                .code(0)
                .message("Success")
                .data(response)
                .build());
    }

    @GetMapping("/search")
    @Operation(summary = "Search furniture by name")
    public ResponseEntity<ApiResponse<Page<FurnitureResponse>>> searchFurniture(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FurnitureResponse> response = furnitureService.searchFurnitureByName(name, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<FurnitureResponse>>builder()
                .code(0)
                .message("Success")
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Update furniture")
    public ResponseEntity<ApiResponse<FurnitureResponse>> updateFurniture(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateFurnitureRequest request) {
        FurnitureResponse response = furnitureService.updateFurniture(id, request);
        return ResponseEntity.ok(ApiResponse.<FurnitureResponse>builder()
                .code(0)
                .message("Furniture updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete furniture")
    public ResponseEntity<ApiResponse<Void>> deleteFurniture(@PathVariable UUID id) {
        furnitureService.deleteFurniture(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(0)
                .message("Furniture deleted successfully")
                .build());
    }
}
