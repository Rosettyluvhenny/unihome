package com.exe.unihome.dto.sku.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SkuAttributeValueResponse {

    private UUID attributeTypeId;
    private String attributeName;
    private String value;
}
