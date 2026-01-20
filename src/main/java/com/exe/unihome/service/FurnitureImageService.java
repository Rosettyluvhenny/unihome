package com.exe.unihome.service;

import com.exe.unihome.dto.furniture.request.FurnitureImageRequest;
import com.exe.unihome.dto.furniture.response.FurnitureImageResponse;

import java.util.List;
import java.util.UUID;

public interface FurnitureImageService {
    List<FurnitureImageResponse> addImages(UUID furnitureId, List<FurnitureImageRequest> requests);
    FurnitureImageResponse setPrimary(UUID furnitureId, UUID imageId);
    void deleteImage(UUID furnitureId, UUID imageId);
    List<FurnitureImageResponse> reorder(UUID furnitureId, List<UUID> orderedIds);
}
