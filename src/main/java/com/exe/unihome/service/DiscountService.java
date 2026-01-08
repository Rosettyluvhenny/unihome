package com.exe.unihome.service;

import com.exe.unihome.dto.discount.request.ApplyDiscountRequest;
import com.exe.unihome.dto.discount.request.CreateDiscountRequest;
import com.exe.unihome.dto.discount.request.UpdateDiscountRequest;
import com.exe.unihome.dto.discount.response.DiscountResponse;

import java.util.List;
import java.util.UUID;

public interface DiscountService {
    DiscountResponse createDiscount(CreateDiscountRequest request);
    DiscountResponse getDiscountById(UUID discountId);
    List<DiscountResponse> getAllDiscounts();
    List<DiscountResponse> getActiveDiscounts();
    DiscountResponse updateDiscount(UUID discountId, UpdateDiscountRequest request);
    void deleteDiscount(UUID discountId);
    void applyDiscountToFurniture(UUID discountId, ApplyDiscountRequest request);
    void removeDiscountFromFurniture(UUID discountId, UUID furnitureId);
}

