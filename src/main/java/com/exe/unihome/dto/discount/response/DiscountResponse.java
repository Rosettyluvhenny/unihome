package com.exe.unihome.dto.discount.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class DiscountResponse {
    private UUID discountId;
    private String name;
    private String description;
    private BigDecimal value;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Calculated fields
    private Boolean isActive;
    private Integer appliedFurnitureCount;
}
