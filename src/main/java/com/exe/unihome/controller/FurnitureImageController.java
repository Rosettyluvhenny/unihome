package com.exe.unihome.controller;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.dto.furniture.request.FurnitureImageRequest;
import com.exe.unihome.dto.furniture.response.FurnitureImageResponse;
import com.exe.unihome.service.FurnitureImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/furniture/{furnitureId}/images")
@RequiredArgsConstructor
@Tag(name = "Furniture Images", description = "Manage furniture images")
public class FurnitureImageController {

    private final FurnitureImageService furnitureImageService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Add images to furniture", description = "Attach one or more image links to a furniture item. The first or flagged image becomes primary if none exists.")
    public ResponseEntity<ApiResponse<List<FurnitureImageResponse>>> addImages(
            @PathVariable UUID furnitureId,
            @Valid @RequestBody List<FurnitureImageRequest> requests) {
        List<FurnitureImageResponse> responses = furnitureImageService.addImages(furnitureId, requests);
        return ResponseEntity.ok(ApiResponse.<List<FurnitureImageResponse>>builder()
                .code(0)
                .message("Images added successfully")
                .data(responses)
                .build());
    }

    @PutMapping("/{imageId}/primary")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Set primary image", description = "Mark an image as primary for the furniture; other images become non-primary.")
    public ResponseEntity<ApiResponse<FurnitureImageResponse>> setPrimary(
            @PathVariable UUID furnitureId,
            @PathVariable UUID imageId) {
        FurnitureImageResponse response = furnitureImageService.setPrimary(furnitureId, imageId);
        return ResponseEntity.ok(ApiResponse.<FurnitureImageResponse>builder()
                .code(0)
                .message("Primary image updated")
                .data(response)
                .build());
    }

    @DeleteMapping("/{imageId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Delete image", description = "Remove an image from the furniture. If primary is deleted, the next image becomes primary.")
    public ResponseEntity<ApiResponse<Void>> deleteImage(
            @PathVariable UUID furnitureId,
            @PathVariable UUID imageId) {
        furnitureImageService.deleteImage(furnitureId, imageId);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(0)
                .message("Image deleted")
                .build());
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN') or hasRole('STAFF')")
    @Operation(summary = "Reorder images", description = "Update display order of images by providing a list of image IDs in desired order.")
    public ResponseEntity<ApiResponse<List<FurnitureImageResponse>>> reorder(
            @PathVariable UUID furnitureId,
            @RequestBody List<UUID> orderedIds) {
        List<FurnitureImageResponse> responses = furnitureImageService.reorder(furnitureId, orderedIds);
        return ResponseEntity.ok(ApiResponse.<List<FurnitureImageResponse>>builder()
                .code(0)
                .message("Images reordered")
                .data(responses)
                .build());
    }
}
