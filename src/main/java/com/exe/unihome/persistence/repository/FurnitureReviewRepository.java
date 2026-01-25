package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.review.FurnitureReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface FurnitureReviewRepository extends JpaRepository<FurnitureReview, UUID> {

    Page<FurnitureReview> findByFurnitureFurnitureId(UUID furnitureId, Pageable pageable);

    Optional<FurnitureReview> findByFurnitureFurnitureIdAndUser_Id(UUID furnitureId, String userId);

}
