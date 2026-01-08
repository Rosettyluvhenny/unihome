package com.exe.unihome.controller;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.dto.discount.request.ApplyDiscountRequest;
import com.exe.unihome.dto.discount.request.CreateDiscountRequest;
import com.exe.unihome.dto.discount.request.UpdateDiscountRequest;
import com.exe.unihome.dto.discount.response.DiscountResponse;
import com.exe.unihome.service.DiscountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/discounts")
@RequiredArgsConstructor
@Tag(name = "Discounts", description = "Discount management APIs")
// @SecurityRequirement(name = "bearerAuth")
public class DiscountController {
    
    private final DiscountService discountService;

    @PostMapping
    // @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Create a new discount", 
               description = "Create a discount with percentage value (0.01-100) and date range. " +
                           "Optional: Include furnitureIds to apply discount immediately upon creation.")
    public ResponseEntity<ApiResponse<DiscountResponse>> createDiscount(
            @Valid @RequestBody CreateDiscountRequest request) {
        DiscountResponse response = discountService.createDiscount(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<DiscountResponse>builder()
                        .code(0)
                        .message("Discount created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get discount by ID")
    public ResponseEntity<ApiResponse<DiscountResponse>> getDiscountById(
            @PathVariable UUID id) {
        DiscountResponse response = discountService.getDiscountById(id);
        return ResponseEntity.ok(ApiResponse.<DiscountResponse>builder()
                .code(0)
                .message("Success")
                .data(response)
                .build());
    }

    @GetMapping
    @Operation(summary = "Get all discounts")
    public ResponseEntity<ApiResponse<List<DiscountResponse>>> getAllDiscounts() {
        List<DiscountResponse> response = discountService.getAllDiscounts();
        return ResponseEntity.ok(ApiResponse.<List<DiscountResponse>>builder()
                .code(0)
                .message("Success")
                .data(response)
                .build());
    }

    @GetMapping("/active")
    @Operation(summary = "Get active discounts", 
               description = "Get discounts that are currently active (today is between start and end date)")
    public ResponseEntity<ApiResponse<List<DiscountResponse>>> getActiveDiscounts() {
        List<DiscountResponse> response = discountService.getActiveDiscounts();
        return ResponseEntity.ok(ApiResponse.<List<DiscountResponse>>builder()
                .code(0)
                .message("Success")
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Update discount", 
               description = "Update discount value and/or dates. Will recalculate all applied furniture prices.")
    public ResponseEntity<ApiResponse<DiscountResponse>> updateDiscount(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDiscountRequest request) {
        DiscountResponse response = discountService.updateDiscount(id, request);
        return ResponseEntity.ok(ApiResponse.<DiscountResponse>builder()
                .code(0)
                .message("Discount updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete discount", 
               description = "Delete discount and remove it from all applied furniture")
    public ResponseEntity<ApiResponse<Void>> deleteDiscount(@PathVariable UUID id) {
        discountService.deleteDiscount(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(0)
                .message("Discount deleted successfully")
                .build());
    }

    @PostMapping("/{discountId}/apply")
    // @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Apply discount to furniture items", 
               description = "Apply this discount to one or more furniture items. Will calculate and update finalPrice.")
    public ResponseEntity<ApiResponse<Void>> applyDiscountToFurniture(
            @PathVariable UUID discountId,
            @Valid @RequestBody ApplyDiscountRequest request) {
        discountService.applyDiscountToFurniture(discountId, request);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(0)
                .message("Discount applied successfully to " + request.getFurnitureIds().size() + " items")
                .build());
    }

    @DeleteMapping("/{discountId}/furniture/{furnitureId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Remove discount from furniture", 
               description = "Remove this discount from a specific furniture item. Will recalculate finalPrice.")
    public ResponseEntity<ApiResponse<Void>> removeDiscountFromFurniture(
            @PathVariable UUID discountId,
            @PathVariable UUID furnitureId) {
        discountService.removeDiscountFromFurniture(discountId, furnitureId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(0)
                .message("Discount removed from furniture successfully")
                .build());
    }
}

