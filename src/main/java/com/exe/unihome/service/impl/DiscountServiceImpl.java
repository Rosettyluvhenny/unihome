package com.exe.unihome.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.discount.request.ApplyDiscountRequest;
import com.exe.unihome.dto.discount.request.CreateDiscountRequest;
import com.exe.unihome.dto.discount.request.UpdateDiscountRequest;
import com.exe.unihome.dto.discount.response.DiscountResponse;
import com.exe.unihome.persistence.entity.Discount;
import com.exe.unihome.persistence.entity.Furniture;
import com.exe.unihome.persistence.entity.FurnitureDiscount;
import com.exe.unihome.persistence.entity.FurnitureSku;
import com.exe.unihome.mapper.DiscountMapper;
import com.exe.unihome.persistence.repository.DiscountRepository;
import com.exe.unihome.persistence.repository.FurnitureDiscountRepository;
import com.exe.unihome.persistence.repository.FurnitureRepository;
import com.exe.unihome.persistence.repository.FurnitureSkuRepository;
import com.exe.unihome.service.DiscountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DiscountServiceImpl implements DiscountService {
    
    private final DiscountRepository discountRepository;
    private final FurnitureRepository furnitureRepository;
    private final FurnitureDiscountRepository furnitureDiscountRepository;
    private final FurnitureSkuRepository skuRepository;
    private final DiscountMapper discountMapper;

    @Override
    @Transactional
    public DiscountResponse createDiscount(CreateDiscountRequest request) {
        // Validate date range
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        
        Discount discount = discountMapper.toEntity(request);
        Discount savedDiscount = discountRepository.save(discount);
        
        log.info("Created discount: {} with value {}%", savedDiscount.getName(), savedDiscount.getValue());
        
        // CÁCH 2: If furnitureIds provided, apply discount immediately
        if (request.getFurnitureIds() != null && !request.getFurnitureIds().isEmpty()) {
            ApplyDiscountRequest applyRequest = new ApplyDiscountRequest();
            applyRequest.setFurnitureIds(request.getFurnitureIds());
            applyDiscountToFurniture(savedDiscount.getDiscountId(), applyRequest);
            log.info("Auto-applied discount {} to {} furniture items", 
                    savedDiscount.getName(), request.getFurnitureIds().size());
        }
        
        return enrichDiscountResponse(discountMapper.toResponse(savedDiscount));
    }

    @Override
    public DiscountResponse getDiscountById(UUID discountId) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new AppException(ErrorCode.DISCOUNT_NOT_FOUND));
        return enrichDiscountResponse(discountMapper.toResponse(discount));
    }

    @Override
    public List<DiscountResponse> getAllDiscounts() {
        return discountRepository.findAll().stream()
                .map(discountMapper::toResponse)
                .map(this::enrichDiscountResponse)
                .toList();
    }

    @Override
    public List<DiscountResponse> getActiveDiscounts() {
        return discountRepository.findActiveDiscounts(LocalDate.now()).stream()
                .map(discountMapper::toResponse)
                .map(this::enrichDiscountResponse)
                .toList();
    }

    @Override
    @Transactional
    public DiscountResponse updateDiscount(UUID discountId, UpdateDiscountRequest request) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new AppException(ErrorCode.DISCOUNT_NOT_FOUND));
        
        boolean valueChanged = false;
        
        if (request.getName() != null) {
            discount.setName(request.getName());
        }
        if (request.getDescription() != null) {
            discount.setDescription(request.getDescription());
        }
        if (request.getValue() != null) {
            valueChanged = !discount.getValue().equals(request.getValue());
            discount.setValue(request.getValue());
        }
        if (request.getStartDate() != null) {
            discount.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            discount.setEndDate(request.getEndDate());
        }
        
        // Validate date range after update
        if (discount.getEndDate().isBefore(discount.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }
        
        Discount updatedDiscount = discountRepository.save(discount);
        
        // If discount value changed, recalculate all furniture prices
        if (valueChanged) {
            recalculateFurniturePrices(updatedDiscount);
        }
        
        log.info("Updated discount: {}", updatedDiscount.getDiscountId());
        return enrichDiscountResponse(discountMapper.toResponse(updatedDiscount));
    }

    @Override
    @Transactional
    public void deleteDiscount(UUID discountId) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new AppException(ErrorCode.DISCOUNT_NOT_FOUND));
        
        // Get all furniture with this discount
        List<FurnitureDiscount> furnitureDiscounts = furnitureDiscountRepository.findByDiscountId(discountId);
        
        // Remove discount from all furniture
        furnitureDiscountRepository.deleteByDiscountId(discountId);
        
        // Recalculate prices for affected furniture
        furnitureDiscounts.forEach(fd -> {
            Furniture furniture = fd.getFurniture();
            recalculateFurniturePrice(furniture);
        });
        
        discountRepository.delete(discount);
        log.info("Deleted discount: {}", discountId);
    }

    @Override
    @Transactional
    public void applyDiscountToFurniture(UUID discountId, ApplyDiscountRequest request) {
        Discount discount = discountRepository.findById(discountId)
                .orElseThrow(() -> new AppException(ErrorCode.DISCOUNT_NOT_FOUND));
        
        // Validate all furniture IDs exist before processing
        List<UUID> furnitureIds = request.getFurnitureIds();
        List<Furniture> furnitureList = furnitureRepository.findAllById(furnitureIds);
        
        if (furnitureList.size() != furnitureIds.size()) {
            Set<UUID> foundIds = furnitureList.stream()
                .map(Furniture::getFurnitureId)
                .collect(Collectors.toSet());
            
            List<UUID> notFoundIds = furnitureIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();
            
            log.error("Furniture IDs not found: {}", notFoundIds);
            throw new AppException(ErrorCode.FURNITURE_NOT_FOUND);
        }
        
        // Apply discount to each furniture
        for (Furniture furniture : furnitureList) {
            // Check if discount already applied
            Optional<FurnitureDiscount> existing = furnitureDiscountRepository
                .findByFurnitureIdAndDiscountId(furniture.getFurnitureId(), discountId);
            
            if (existing.isPresent()) {
                log.warn("Discount {} already applied to furniture {}, skipping", 
                    discount.getName(), furniture.getName());
                continue;
            }
            
            // Create FurnitureDiscount relationship
            FurnitureDiscount furnitureDiscount = new FurnitureDiscount();
            furnitureDiscount.setFurniture(furniture);
            furnitureDiscount.setDiscount(discount);
            furnitureDiscountRepository.save(furnitureDiscount);
            
            // Recalculate furniture price
            recalculateFurniturePrice(furniture);
            
            log.info("Applied discount {} to furniture {}", discount.getName(), furniture.getName());
        }
    }

    @Override
    @Transactional
    public void removeDiscountFromFurniture(UUID discountId, UUID furnitureId) {
        Furniture furniture = furnitureRepository.findById(furnitureId)
                .orElseThrow(() -> new AppException(ErrorCode.FURNITURE_NOT_FOUND));
        
        FurnitureDiscount furnitureDiscount = furnitureDiscountRepository
                .findByFurnitureIdAndDiscountId(furnitureId, discountId)
                .orElseThrow(() -> new AppException(ErrorCode.DISCOUNT_NOT_FOUND));
        
        furnitureDiscountRepository.deleteByFurnitureIdAndDiscountId(furnitureId, discountId);
        
        // Recalculate price after removing discount
        recalculateFurniturePrice(furniture);
        
        log.info("Removed discount {} from furniture {}", discountId, furnitureId);
    }
    
    /**
     * Recalculate finalPrice for a furniture item and all its SKUs based on ACTIVE discounts
     */
    private void recalculateFurniturePrice(Furniture furniture) {
        // Only consider active discounts (within date range)
        List<FurnitureDiscount> activeDiscounts = furnitureDiscountRepository
            .findActiveDiscountsByFurnitureId(furniture.getFurnitureId(), LocalDate.now());
        
        if (activeDiscounts.isEmpty()) {
            // No active discounts - finalPrice = price
            furniture.setFinalPrice(furniture.getPrice());
            furniture.setHasDiscount(false);

            // Reset all SKUs
            List<FurnitureSku> skus = skuRepository.findByFurnitureFurnitureId(furniture.getFurnitureId());
            for (FurnitureSku sku : skus) {
                sku.setFinalPrice(sku.getPrice());
                sku.setHasDiscount(false);
            }
            skuRepository.saveAll(skus);
        } else {
            // Find highest active discount percentage
            BigDecimal highestDiscount = activeDiscounts.stream()
                    .map(fd -> fd.getDiscount().getValue())
                    .max(Comparator.naturalOrder())
                    .orElse(BigDecimal.ZERO);
            
            // Calculate furniture: finalPrice = price - (price * discount / 100)
            BigDecimal discountAmount = furniture.getPrice()
                    .multiply(highestDiscount)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            
            BigDecimal finalPrice = furniture.getPrice().subtract(discountAmount);
            
            furniture.setFinalPrice(finalPrice);
            furniture.setHasDiscount(true);

            // Apply same discount percentage to all SKUs
            List<FurnitureSku> skus = skuRepository.findByFurnitureFurnitureId(furniture.getFurnitureId());
            for (FurnitureSku sku : skus) {
                BigDecimal skuDiscountAmount = sku.getPrice()
                        .multiply(highestDiscount)
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                sku.setFinalPrice(sku.getPrice().subtract(skuDiscountAmount));
                sku.setHasDiscount(true);
            }
            skuRepository.saveAll(skus);
            
            log.debug("Recalculated price for furniture {} and {} SKUs: {}% off", 
                furniture.getName(), skus.size(), highestDiscount);
        }
        
        furnitureRepository.save(furniture);
    }
    
    /**
     * Recalculate prices for all furniture with this discount
     */
    private void recalculateFurniturePrices(Discount discount) {
        List<FurnitureDiscount> furnitureDiscounts = furnitureDiscountRepository.findByDiscountId(discount.getDiscountId());
        furnitureDiscounts.forEach(fd -> recalculateFurniturePrice(fd.getFurniture()));
        log.info("Recalculated prices for {} furniture items", furnitureDiscounts.size());
    }
    
    /**
     * Enrich DiscountResponse with calculated fields
     */
    private DiscountResponse enrichDiscountResponse(DiscountResponse response) {
        // Calculate isActive
        LocalDate today = LocalDate.now();
        boolean isActive = !today.isBefore(response.getStartDate()) && !today.isAfter(response.getEndDate());
        response.setIsActive(isActive);
        
        // Count applied furniture
        int count = furnitureDiscountRepository.findByDiscountId(response.getDiscountId()).size();
        response.setAppliedFurnitureCount(count);
        
        return response;
    }
}

