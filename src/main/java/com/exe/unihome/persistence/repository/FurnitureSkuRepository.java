package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.FurnitureSku;
import com.exe.unihome.persistence.enums.FurnitureStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FurnitureSkuRepository extends JpaRepository<FurnitureSku, UUID> {

    List<FurnitureSku> findByFurnitureFurnitureId(UUID furnitureId);

    Optional<FurnitureSku> findBySkuId(UUID skuId);

    boolean existsBySkuCode(String skuCode);

    Optional<FurnitureSku> findBySkuCode(String skuCode);

    List<FurnitureSku> findByFurnitureFurnitureIdAndStatus(UUID furnitureId, FurnitureStatus status);
}
