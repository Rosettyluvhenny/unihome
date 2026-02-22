package com.exe.unihome.controller;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.dto.sku.request.CreateSkuRequest;
import com.exe.unihome.dto.sku.request.UpdateSkuRequest;
import com.exe.unihome.dto.sku.response.SkuResponse;
import com.exe.unihome.service.FurnitureSkuService;
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
@RequestMapping("/furniture")
@RequiredArgsConstructor
@Tag(name = "Furniture SKU", description = "Manage furniture variants / SKUs")
@SecurityRequirement(name = "bearerAuth")
public class FurnitureSkuController {

    private final FurnitureSkuService furnitureSkuService;

    @GetMapping("/{furnitureId}/skus")
    @Operation(summary = "List all SKUs for a furniture item")
    public ResponseEntity<ApiResponse<List<SkuResponse>>> getSkus(
            @PathVariable UUID furnitureId) {
        List<SkuResponse> skus = furnitureSkuService.getSkusByFurnitureId(furnitureId);
        return ResponseEntity.ok(ApiResponse.<List<SkuResponse>>builder()
                .code(0)
                .message("Success")
                .data(skus)
                .build());
    }

    @GetMapping("/skus/{skuId}")
    @Operation(summary = "Get a single SKU by ID")
    public ResponseEntity<ApiResponse<SkuResponse>> getSku(
            @PathVariable UUID skuId) {
        SkuResponse sku = furnitureSkuService.getSkuById(skuId);
        return ResponseEntity.ok(ApiResponse.<SkuResponse>builder()
                .code(0)
                .message("Success")
                .data(sku)
                .build());
    }

    @PostMapping("/{furnitureId}/skus")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Create a new SKU for a furniture item")
    public ResponseEntity<ApiResponse<SkuResponse>> createSku(
            @PathVariable UUID furnitureId,
            @Valid @RequestBody CreateSkuRequest request) {
        SkuResponse sku = furnitureSkuService.createSku(furnitureId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<SkuResponse>builder()
                        .code(0)
                        .message("SKU created successfully")
                        .data(sku)
                        .build());
    }

    @PutMapping("/skus/{skuId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Update a SKU")
    public ResponseEntity<ApiResponse<SkuResponse>> updateSku(
            @PathVariable UUID skuId,
            @Valid @RequestBody UpdateSkuRequest request) {
        SkuResponse sku = furnitureSkuService.updateSku(skuId, request);
        return ResponseEntity.ok(ApiResponse.<SkuResponse>builder()
                .code(0)
                .message("SKU updated successfully")
                .data(sku)
                .build());
    }

    @DeleteMapping("/skus/{skuId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a SKU")
    public ResponseEntity<ApiResponse<Void>> deleteSku(@PathVariable UUID skuId) {
        furnitureSkuService.deleteSku(skuId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(0)
                .message("SKU deleted successfully")
                .build());
    }
}
