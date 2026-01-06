package com.exe.unihome.dto.discount.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class ApplyDiscountRequest {
    
    @NotEmpty(message = "Furniture IDs list cannot be empty")
    private List<UUID> furnitureIds;
}
