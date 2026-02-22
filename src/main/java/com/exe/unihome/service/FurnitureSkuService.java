package com.exe.unihome.service;

import com.exe.unihome.dto.sku.request.CreateSkuRequest;
import com.exe.unihome.dto.sku.request.UpdateSkuRequest;
import com.exe.unihome.dto.sku.response.SkuResponse;

import java.util.List;
import java.util.UUID;

public interface FurnitureSkuService {

    List<SkuResponse> getSkusByFurnitureId(UUID furnitureId);

    SkuResponse getSkuById(UUID skuId);

    SkuResponse createSku(UUID furnitureId, CreateSkuRequest request);

    SkuResponse updateSku(UUID skuId, UpdateSkuRequest request);

    void deleteSku(UUID skuId);
}
