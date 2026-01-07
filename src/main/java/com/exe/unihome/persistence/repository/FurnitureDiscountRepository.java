package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.FurnitureDiscount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FurnitureDiscountRepository extends JpaRepository<FurnitureDiscount, UUID> {
    
    @Query("SELECT fd FROM FurnitureDiscount fd WHERE fd.furniture.furnitureId = :furnitureId")
    List<FurnitureDiscount> findByFurnitureId(@Param("furnitureId") UUID furnitureId);
    
    @Query("SELECT fd FROM FurnitureDiscount fd WHERE fd.discount.discountId = :discountId")
    List<FurnitureDiscount> findByDiscountId(@Param("discountId") UUID discountId);
    
    @Query("SELECT fd FROM FurnitureDiscount fd WHERE fd.furniture.furnitureId = :furnitureId AND fd.discount.discountId = :discountId")
    Optional<FurnitureDiscount> findByFurnitureIdAndDiscountId(@Param("furnitureId") UUID furnitureId, @Param("discountId") UUID discountId);
    
    @Modifying
    @Query("DELETE FROM FurnitureDiscount fd WHERE fd.furniture.furnitureId = :furnitureId AND fd.discount.discountId = :discountId")
    void deleteByFurnitureIdAndDiscountId(@Param("furnitureId") UUID furnitureId, @Param("discountId") UUID discountId);
    
    @Modifying
    @Query("DELETE FROM FurnitureDiscount fd WHERE fd.discount.discountId = :discountId")
    void deleteByDiscountId(@Param("discountId") UUID discountId);
}

