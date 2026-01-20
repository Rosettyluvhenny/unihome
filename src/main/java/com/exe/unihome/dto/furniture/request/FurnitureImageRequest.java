package com.exe.unihome.dto.furniture.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class FurnitureImageRequest {
    @NotBlank(message = "Image URL is required")
    private String imageUrl;

    private Boolean isPrimary;

    private Integer displayOrder;
}
