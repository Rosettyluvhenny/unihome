package com.exe.unihome.persistence.repository;

import com.exe.unihome.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DiscountRepository extends JpaRepository<Discount, UUID> {
    
    @Query("SELECT d FROM Discount d WHERE :today BETWEEN d.startDate AND d.endDate")
    List<Discount> findActiveDiscounts(@Param("today") LocalDate today);
    
    @Query("SELECT d FROM Discount d WHERE d.endDate < :today")
    List<Discount> findExpiredDiscounts(@Param("today") LocalDate today);
    
    @Query("SELECT d FROM Discount d WHERE d.startDate > :today")
    List<Discount> findUpcomingDiscounts(@Param("today") LocalDate today);
}

