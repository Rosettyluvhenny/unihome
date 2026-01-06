package com.exe.unihome.dto.discount.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
public class CreateDiscountRequest {
    
    @NotBlank(message = "Discount name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Discount value is required")
    @DecimalMin(value = "0.01", message = "Discount value must be at least 0.01%")
    @DecimalMax(value = "100.00", message = "Discount value must not exceed 100%")
    private BigDecimal value;
    
    @NotNull(message = "Start date is required")
    private LocalDate startDate;
    
    @NotNull(message = "End date is required")
    private LocalDate endDate;
    
    // CÁCH 2: Optional field - if provided, auto-apply discount to these furniture items
    private List<UUID> furnitureIds;
}
