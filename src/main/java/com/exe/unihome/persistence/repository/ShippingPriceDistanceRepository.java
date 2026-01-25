package com.exe.unihome.persistence.repository;

import com.exe.unihome.persistence.entity.shipping.ShippingPriceDistance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ShippingPriceDistanceRepository extends JpaRepository<ShippingPriceDistance, Integer> {

    @Query("SELECT s FROM ShippingPriceDistance s " +
        "WHERE s.minDistanceKm <= :distance " +
        "AND (s.maxDistanceKm IS NULL OR s.maxDistanceKm >= :distance) " +
        "ORDER BY s.minDistanceKm DESC")
    List<ShippingPriceDistance> findTiersForDistance(@Param("distance") int distance);

    Optional<ShippingPriceDistance> findFirstByMaxDistanceKmIsNullOrderByMinDistanceKmAsc();
}
