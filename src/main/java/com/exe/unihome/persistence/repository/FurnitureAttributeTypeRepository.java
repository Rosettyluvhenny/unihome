package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.FurnitureAttributeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FurnitureAttributeTypeRepository extends JpaRepository<FurnitureAttributeType, UUID> {

    List<FurnitureAttributeType> findByFurnitureFurnitureIdOrderByDisplayOrderAsc(UUID furnitureId);

    boolean existsByFurnitureFurnitureIdAndName(UUID furnitureId, String name);

    void deleteAllByFurnitureFurnitureId(UUID furnitureId);
}
