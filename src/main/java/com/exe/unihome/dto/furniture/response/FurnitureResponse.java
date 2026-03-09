package com.exe.unihome.dto.furniture.response;

import com.exe.unihome.dto.sku.response.SkuResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FurnitureResponse {
    
    private UUID furnitureId;
    private UUID categoryId;
    private String categoryName;
    private String name;
    private BigDecimal price;
    private BigDecimal finalPrice;
    private Integer stock;
    private String status;
    private Boolean hasDiscount;
    private String primaryImageUrl;
    private List<FurnitureImageResponse> images;
    private List<SkuResponse> skus;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
