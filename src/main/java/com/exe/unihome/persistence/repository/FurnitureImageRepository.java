package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.FurnitureImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FurnitureImageRepository extends JpaRepository<FurnitureImage, UUID> {

    List<FurnitureImage> findByFurnitureFurnitureIdOrderByDisplayOrderAscCreatedAtAsc(UUID furnitureId);

    Optional<FurnitureImage> findByFurnitureFurnitureIdAndIsPrimaryTrue(UUID furnitureId);

    @Modifying(clearAutomatically = true)
    @Query("delete from FurnitureImage fi where fi.furniture.furnitureId = :furnitureId")
    void deleteByFurnitureId(@Param("furnitureId") UUID furnitureId);

    @Modifying(clearAutomatically = true)
    @Query("update FurnitureImage fi set fi.isPrimary = false where fi.furniture.furnitureId = :furnitureId and fi.isPrimary = true")
    void clearPrimaryFlag(@Param("furnitureId") UUID furnitureId);

 
}
