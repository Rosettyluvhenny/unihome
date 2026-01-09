package com.exe.unihome.dto.furniture.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateFurnitureRequest {
    
    @NotNull(message = "Category ID is required")
    private UUID categoryId;
    
    @NotBlank(message = "Furniture name is required")
    private String name;
    
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;
    
    @NotNull(message = "Stock is required")
    @PositiveOrZero(message = "Stock must be zero or positive")
    private Integer stock;
    
    @NotNull(message = "Status is required")
    private String status; // AVAILABLE, OUT_OF_STOCK, DISCONTINUED
}
