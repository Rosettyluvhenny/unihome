package com.exe.unihome.dto.sku.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateSkuRequest {

    private String skuCode;

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @PositiveOrZero(message = "Stock must be zero or positive")
    private Integer stock;

    private String status;

    /**
     * Map of attributeTypeId -> value (replaces all existing attribute values).
     */
    private Map<UUID, String> attributes;

    /**
     * Image URL for this SKU variant.
     */
    private String imageUrl;
}
