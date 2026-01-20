package com.exe.unihome.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.furniture.request.FurnitureImageRequest;
import com.exe.unihome.dto.furniture.response.FurnitureImageResponse;
import com.exe.unihome.mapper.FurnitureImageMapper;
import com.exe.unihome.persistence.entity.Furniture;
import com.exe.unihome.persistence.entity.FurnitureImage;
import com.exe.unihome.persistence.repository.FurnitureImageRepository;
import com.exe.unihome.persistence.repository.FurnitureRepository;
import com.exe.unihome.service.FurnitureImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FurnitureImageServiceImpl implements FurnitureImageService {

    private final FurnitureRepository furnitureRepository;
    private final FurnitureImageRepository furnitureImageRepository;
    private final FurnitureImageMapper furnitureImageMapper;

    @Override
    @Transactional
    public List<FurnitureImageResponse> addImages(UUID furnitureId, List<FurnitureImageRequest> requests) {
        Furniture furniture = furnitureRepository.findById(furnitureId)
                .orElseThrow(() -> new AppException(ErrorCode.FURNITURE_NOT_FOUND));

        if (requests == null || requests.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        // Normalize current images to compute next order
        List<FurnitureImage> current = furnitureImageRepository
            .findByFurnitureFurnitureIdOrderByDisplayOrderAscCreatedAtAsc(furnitureId);
        boolean existingHasPrimary = current.stream().anyMatch(img -> Boolean.TRUE.equals(img.getIsPrimary()));
        int startOrder = current.isEmpty() ? 0 : current.get(current.size() - 1).getDisplayOrder() + 1;

        List<FurnitureImage> toSave = new ArrayList<>();
        for (int i = 0; i < requests.size(); i++) {
            FurnitureImageRequest req = requests.get(i);
            FurnitureImage image = FurnitureImage.builder()
                    .furniture(furniture)
                    .imageUrl(req.getImageUrl())
                    .isPrimary(Boolean.TRUE.equals(req.getIsPrimary()))
                    .displayOrder(req.getDisplayOrder() != null ? req.getDisplayOrder() : startOrder + i)
                    .build();
            toSave.add(image);
        }

        // Ensure only one primary: if none set, pick first new; if multiple set, keep the first flagged
        boolean newPrimaryAssigned = ensureSinglePrimary(toSave, existingHasPrimary);

        if (newPrimaryAssigned && existingHasPrimary) {
            furnitureImageRepository.clearPrimaryFlag(furnitureId);
            furnitureImageRepository.flush();
            current.forEach(img -> img.setIsPrimary(false));
        }

        List<FurnitureImage> saved = furnitureImageRepository.saveAll(toSave);
        saved.addAll(current); // include existing for response ordering

        return saved.stream()
            .sorted(Comparator.comparing(FurnitureImage::getDisplayOrder)
                .thenComparing(img ->
                    img.getCreatedAt() != null ? img.getCreatedAt() : LocalDateTime.MIN))
            .map(furnitureImageMapper::toResponse)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public FurnitureImageResponse setPrimary(UUID furnitureId, UUID imageId) {
        Furniture furniture = furnitureRepository.findById(furnitureId)
                .orElseThrow(() -> new AppException(ErrorCode.FURNITURE_NOT_FOUND));

        FurnitureImage target = furnitureImageRepository.findById(imageId)
                .filter(img -> img.getFurniture().getFurnitureId().equals(furniture.getFurnitureId()))
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));

        // Reset existing primary flag in DB before promoting the requested image
        furnitureImageRepository.clearPrimaryFlag(furnitureId);
        target.setIsPrimary(true);
        FurnitureImage saved = furnitureImageRepository.save(target);

        return furnitureImageMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteImage(UUID furnitureId, UUID imageId) {
        FurnitureImage target = furnitureImageRepository.findById(imageId)
                .filter(img -> img.getFurniture().getFurnitureId().equals(furnitureId))
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_REQUEST));

        boolean wasPrimary = Boolean.TRUE.equals(target.getIsPrimary());
        furnitureImageRepository.delete(target);

        if (wasPrimary) {
            // Promote next image as primary if any
            List<FurnitureImage> remaining = furnitureImageRepository
                    .findByFurnitureFurnitureIdOrderByDisplayOrderAscCreatedAtAsc(furnitureId);
            if (!remaining.isEmpty()) {
                FurnitureImage first = remaining.get(0);
                first.setIsPrimary(true);
                furnitureImageRepository.save(first);
            }
        }
    }

    @Override
    @Transactional
    public List<FurnitureImageResponse> reorder(UUID furnitureId, List<UUID> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        List<FurnitureImage> images = furnitureImageRepository
                .findByFurnitureFurnitureIdOrderByDisplayOrderAscCreatedAtAsc(furnitureId);

        // Validate ownership and apply new order
        for (int i = 0; i < orderedIds.size(); i++) {
            UUID id = orderedIds.get(i);
            Optional<FurnitureImage> match = images.stream()
                    .filter(img -> img.getImageId().equals(id))
                    .findFirst();
            if (match.isEmpty()) {
                throw new AppException(ErrorCode.INVALID_REQUEST);
            }
            match.get().setDisplayOrder(i);
        }
        List<FurnitureImage> saved = furnitureImageRepository.saveAll(images);
        return saved.stream()
            .sorted(Comparator.comparing(FurnitureImage::getDisplayOrder)
                .thenComparing(img ->
                    img.getCreatedAt() != null ? img.getCreatedAt() : LocalDateTime.MIN))
            .map(furnitureImageMapper::toResponse)
            .collect(Collectors.toList());
    }

    private boolean ensureSinglePrimary(List<FurnitureImage> newImages, boolean existingHasPrimary) {
        boolean anyPrimary = newImages.stream().anyMatch(img -> Boolean.TRUE.equals(img.getIsPrimary()));
        if (!anyPrimary) {
            if (!existingHasPrimary && !newImages.isEmpty()) {
                newImages.get(0).setIsPrimary(true);
                return true;
            }
            return false;
        }
        // If multiple flagged primary, keep the first flagged
        boolean primaryAssigned = false;
        for (FurnitureImage img : newImages) {
            if (Boolean.TRUE.equals(img.getIsPrimary())) {
                if (primaryAssigned) {
                    img.setIsPrimary(false);
                } else {
                    primaryAssigned = true;
                }
            }
        }
        return primaryAssigned;
    }
}
