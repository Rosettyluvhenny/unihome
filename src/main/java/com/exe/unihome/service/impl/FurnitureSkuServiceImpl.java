package com.exe.unihome.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.sku.request.CreateSkuRequest;
import com.exe.unihome.dto.sku.request.UpdateSkuRequest;
import com.exe.unihome.dto.sku.response.SkuResponse;
import com.exe.unihome.mapper.FurnitureSkuMapper;
import com.exe.unihome.persistence.entity.Furniture;
import com.exe.unihome.persistence.entity.FurnitureSku;
import com.exe.unihome.persistence.enums.FurnitureStatus;
import com.exe.unihome.persistence.repository.FurnitureRepository;
import com.exe.unihome.persistence.repository.FurnitureSkuRepository;
import com.exe.unihome.service.FurnitureSkuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FurnitureSkuServiceImpl implements FurnitureSkuService {

    private final FurnitureSkuRepository skuRepository;
    private final FurnitureRepository furnitureRepository;
    private final FurnitureSkuMapper skuMapper;

    @Override
    @Transactional(readOnly = true)
    public List<SkuResponse> getSkusByFurnitureId(UUID furnitureId) {
        if (!furnitureRepository.existsById(furnitureId)) {
            throw new AppException(ErrorCode.FURNITURE_NOT_FOUND);
        }
        List<FurnitureSku> skus = skuRepository.findByFurnitureFurnitureId(furnitureId);
        return skus.stream()
                .map(skuMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SkuResponse getSkuById(UUID skuId) {
        FurnitureSku sku = skuRepository.findBySkuId(skuId)
                .orElseThrow(() -> new AppException(ErrorCode.SKU_NOT_FOUND));
        return skuMapper.toResponse(sku);
    }

    @Override
    @Transactional
    public SkuResponse createSku(UUID furnitureId, CreateSkuRequest request) {
        Furniture furniture = furnitureRepository.findById(furnitureId)
                .orElseThrow(() -> new AppException(ErrorCode.FURNITURE_NOT_FOUND));

        if (skuRepository.existsBySkuCode(request.getSkuCode())) {
            throw new AppException(ErrorCode.SKU_CODE_EXISTS);
        }

        FurnitureStatus status = FurnitureStatus.AVAILABLE;
        if (request.getStatus() != null) {
            try {
                status = FurnitureStatus.valueOf(request.getStatus().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
        }

        FurnitureSku sku = FurnitureSku.builder()
                .furniture(furniture)
                .skuCode(request.getSkuCode())
                .price(request.getPrice())
                .finalPrice(request.getPrice()) // No discount initially
                .stock(request.getStock())
                .status(status)
                .hasDiscount(false)
                .imageUrl(request.getImageUrl())
                .build();

        FurnitureSku savedSku = skuRepository.save(sku);

        // Sync furniture stock = SUM of all SKU stocks
        syncFurnitureStock(furniture);

        log.info("Created SKU {} for furniture {}", savedSku.getSkuCode(), furniture.getName());

        // Re-fetch to get full entity graph
        return skuMapper.toResponse(
                skuRepository.findBySkuId(savedSku.getSkuId()).orElse(savedSku));
    }

    @Override
    @Transactional
    public SkuResponse updateSku(UUID skuId, UpdateSkuRequest request) {
        FurnitureSku sku = skuRepository.findBySkuId(skuId)
                .orElseThrow(() -> new AppException(ErrorCode.SKU_NOT_FOUND));

        if (request.getSkuCode() != null && !request.getSkuCode().equals(sku.getSkuCode())) {
            if (skuRepository.existsBySkuCode(request.getSkuCode())) {
                throw new AppException(ErrorCode.SKU_CODE_EXISTS);
            }
            sku.setSkuCode(request.getSkuCode());
        }

        if (request.getPrice() != null) {
            sku.setPrice(request.getPrice());
            if (!Boolean.TRUE.equals(sku.getHasDiscount())) {
                sku.setFinalPrice(request.getPrice());
            }
        }

        if (request.getStock() != null) {
            sku.setStock(request.getStock());
        }

        if (request.getStatus() != null) {
            try {
                sku.setStatus(FurnitureStatus.valueOf(request.getStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
        }

        if (request.getImageUrl() != null) {
            sku.setImageUrl(request.getImageUrl());
        }

        FurnitureSku savedSku = skuRepository.save(sku);

        // Sync furniture stock
        syncFurnitureStock(sku.getFurniture());

        log.info("Updated SKU {}", savedSku.getSkuCode());

        return skuMapper.toResponse(
                skuRepository.findBySkuId(savedSku.getSkuId()).orElse(savedSku));
    }

    @Override
    @Transactional
    public void deleteSku(UUID skuId) {
        FurnitureSku sku = skuRepository.findBySkuId(skuId)
                .orElseThrow(() -> new AppException(ErrorCode.SKU_NOT_FOUND));

        Furniture furniture = sku.getFurniture();
        skuRepository.delete(sku);

        // Sync furniture stock
        syncFurnitureStock(furniture);

        log.info("Deleted SKU {}", sku.getSkuCode());
    }

    // ── helpers ──────────────────────────────────────────────

    private void syncFurnitureStock(Furniture furniture) {
        List<FurnitureSku> allSkus = skuRepository.findByFurnitureFurnitureIdAndStatus(
                furniture.getFurnitureId(), FurnitureStatus.AVAILABLE);
        int totalStock = allSkus.stream().mapToInt(FurnitureSku::getStock).sum();
        furniture.setStock(totalStock);
        furnitureRepository.save(furniture);
    }
}
