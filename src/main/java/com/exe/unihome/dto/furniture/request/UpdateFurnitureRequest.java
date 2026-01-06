package com.exe.unihome.dto.furniture.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class UpdateFurnitureRequest {
    
    private UUID categoryId;
    
    private String name;
    
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    @PositiveOrZero(message = "Stock must be zero or positive")
    private Integer stock;
    
    private String status; // AVAILABLE, OUT_OF_STOCK, DISCONTINUED
}
