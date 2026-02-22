package com.exe.unihome.dto.sku.response;

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
public class SkuResponse {

    private UUID skuId;
    private String skuCode;
    private BigDecimal price;
    private BigDecimal finalPrice;
    private Integer stock;
    private String status;
    private Boolean hasDiscount;
    private String imageUrl;
    private List<SkuAttributeValueResponse> attributes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
