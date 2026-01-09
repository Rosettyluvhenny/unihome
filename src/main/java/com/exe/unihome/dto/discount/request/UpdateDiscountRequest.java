package com.exe.unihome.dto.discount.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class UpdateDiscountRequest {
    
    private String name;
    
    private String description;
    
    @DecimalMin(value = "0.01", message = "Discount value must be at least 0.01%")
    @DecimalMax(value = "100.00", message = "Discount value must not exceed 100%")
    private BigDecimal value;
    
    private LocalDate startDate;
    
    private LocalDate endDate;
}
