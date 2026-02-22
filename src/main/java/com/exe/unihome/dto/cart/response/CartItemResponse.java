package com.exe.unihome.dto.cart.response;

import com.exe.unihome.dto.sku.response.SkuAttributeValueResponse;
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
public class CartItemResponse {

    private UUID cartItemId;
    private UUID furnitureId;
    private String furnitureName;
    private UUID skuId;
    private String skuCode;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal lineTotal;
    private String primaryImageUrl;
    private List<SkuAttributeValueResponse> skuAttributes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
