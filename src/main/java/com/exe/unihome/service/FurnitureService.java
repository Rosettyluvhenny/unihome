package com.exe.unihome.service;

import com.exe.unihome.dto.furniture.request.CreateFurnitureRequest;
import com.exe.unihome.dto.furniture.request.UpdateFurnitureRequest;
import com.exe.unihome.dto.furniture.response.FurnitureResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface FurnitureService {
    
    FurnitureResponse createFurniture(CreateFurnitureRequest request);
    
    FurnitureResponse getFurnitureById(UUID id);
    
    Page<FurnitureResponse> getAllFurniture(Pageable pageable);
    
    List<FurnitureResponse> getFurnitureByCategory(UUID categoryId);
    
    Page<FurnitureResponse> searchFurnitureByName(String name, Pageable pageable);
    
    FurnitureResponse updateFurniture(UUID id, UpdateFurnitureRequest request);
    
    void deleteFurniture(UUID id);
}
