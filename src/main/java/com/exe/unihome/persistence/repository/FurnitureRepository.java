package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.Furniture;
import com.exe.unihome.persistence.enums.FurnitureStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FurnitureRepository extends JpaRepository<Furniture, UUID> {
    
    List<Furniture> findByCategoryCategoryId(UUID categoryId);
    
    List<Furniture> findByStatus(FurnitureStatus status);
    
    Page<Furniture> findByNameContainingIgnoreCase(String name, Pageable pageable);
    
    @Query("SELECT f FROM Furniture f WHERE f.category.categoryId = :categoryId AND f.status = :status")
    List<Furniture> findByCategoryAndStatus(@Param("categoryId") UUID categoryId, 
                                           @Param("status") FurnitureStatus status);
    
    boolean existsByNameAndCategoryCategoryId(String name, UUID categoryId);
}
