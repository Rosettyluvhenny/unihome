package com.exe.unihome.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.furniture.request.CreateFurnitureRequest;
import com.exe.unihome.dto.furniture.request.UpdateFurnitureRequest;
import com.exe.unihome.dto.furniture.response.FurnitureResponse;
import com.exe.unihome.entity.Category;
import com.exe.unihome.entity.Furniture;
import com.exe.unihome.entity.FurnitureStatus;
import com.exe.unihome.mapper.FurnitureMapper;
import com.exe.unihome.persistence.repository.CategoryRepository;
import com.exe.unihome.persistence.repository.FurnitureRepository;
import com.exe.unihome.service.FurnitureService;
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
public class FurnitureServiceImpl implements FurnitureService {
    
    private final FurnitureRepository furnitureRepository;
    private final CategoryRepository categoryRepository;
    private final FurnitureMapper furnitureMapper;

    @Override
    @Transactional
    public FurnitureResponse createFurniture(CreateFurnitureRequest request) {
        log.info("Creating furniture: {}", request.getName());
        
        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
        
        // Check duplicate name in same category
        if (furnitureRepository.existsByNameAndCategoryCategoryId(request.getName(), request.getCategoryId())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        // Parse status
        FurnitureStatus status;
        try {
            status = FurnitureStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        Furniture furniture = Furniture.builder()
                .furnitureId(UUID.randomUUID())
                .category(category)
                .name(request.getName())
                .price(request.getPrice())
                .finalPrice(request.getPrice()) // Initially no discount
                .stock(request.getStock())
                .status(status)
                .hasDiscount(false)
                .build();
        
        Furniture savedFurniture = furnitureRepository.save(furniture);
        log.info("Furniture created successfully with ID: {}", savedFurniture.getFurnitureId());
        
        return furnitureMapper.toResponse(savedFurniture);
    }

    @Override
    public FurnitureResponse getFurnitureById(UUID id) {
        log.info("Getting furniture by ID: {}", id);
        
        Furniture furniture = furnitureRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
        
        return furnitureMapper.toResponse(furniture);
    }

    @Override
    public Page<FurnitureResponse> getAllFurniture(Pageable pageable) {
        log.info("Getting all furniture with pagination");
        
        return furnitureRepository.findAll(pageable)
                .map(furnitureMapper::toResponse);
    }

    @Override
    public List<FurnitureResponse> getFurnitureByCategory(UUID categoryId) {
        log.info("Getting furniture by category ID: {}", categoryId);
        
        // Validate category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        return furnitureRepository.findByCategoryCategoryId(categoryId)
                .stream()
                .map(furnitureMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<FurnitureResponse> searchFurnitureByName(String name, Pageable pageable) {
        log.info("Searching furniture by name: {}", name);
        
        return furnitureRepository.findByNameContainingIgnoreCase(name, pageable)
                .map(furnitureMapper::toResponse);
    }

    @Override
    @Transactional
    public FurnitureResponse updateFurniture(UUID id, UpdateFurnitureRequest request) {
        log.info("Updating furniture with ID: {}", id);
        
        Furniture furniture = furnitureRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
        
        // Update category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));
            furniture.setCategory(category);
        }
        
        // Update name if provided
        if (request.getName() != null && !request.getName().isBlank()) {
            furniture.setName(request.getName());
        }
        
        // Update price if provided
        if (request.getPrice() != null) {
            furniture.setPrice(request.getPrice());
            // Recalculate final price if no discount
            if (!furniture.getHasDiscount()) {
                furniture.setFinalPrice(request.getPrice());
            }
        }
        
        // Update stock if provided
        if (request.getStock() != null) {
            furniture.setStock(request.getStock());
        }
        
        // Update status if provided
        if (request.getStatus() != null) {
            try {
                FurnitureStatus status = FurnitureStatus.valueOf(request.getStatus().toUpperCase());
                furniture.setStatus(status);
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
        }
        
        Furniture updatedFurniture = furnitureRepository.save(furniture);
        log.info("Furniture updated successfully: {}", id);
        
        return furnitureMapper.toResponse(updatedFurniture);
    }

    @Override
    @Transactional
    public void deleteFurniture(UUID id) {
        log.info("Deleting furniture with ID: {}", id);
        
        if (!furnitureRepository.existsById(id)) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }
        
        furnitureRepository.deleteById(id);
        log.info("Furniture deleted successfully: {}", id);
    }
}
