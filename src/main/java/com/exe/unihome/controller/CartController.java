package com.exe.unihome.controller;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.dto.cart.request.CartItemRequest;
import com.exe.unihome.dto.cart.request.UpdateCartItemRequest;
import com.exe.unihome.dto.cart.response.CartResponse;
import com.exe.unihome.service.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Customer cart management APIs")
@SecurityRequirement(name = "bearerAuth")
public class CartController {

    private final CartService cartService;

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Get current cart")
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        CartResponse response = cartService.getCart(authentication.getName());
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .code(0)
                .message("Success")
                .data(response)
                .build());
    }

    @PostMapping("/items")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Add item to cart")
    public ResponseEntity<ApiResponse<CartResponse>> addItem(
            Authentication authentication,
            @Valid @RequestBody CartItemRequest request) {
        CartResponse response = cartService.addItem(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .code(0)
                .message("Item added")
                .data(response)
                .build());
    }

    @PutMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Update cart item quantity")
    public ResponseEntity<ApiResponse<CartResponse>> updateItem(
            Authentication authentication,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {
        CartResponse response = cartService.updateItem(authentication.getName(), itemId, request);
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .code(0)
                .message("Item updated")
                .data(response)
                .build());
    }

    @DeleteMapping("/items/{itemId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Remove item from cart")
    public ResponseEntity<ApiResponse<CartResponse>> removeItem(
            Authentication authentication,
            @PathVariable UUID itemId) {
        CartResponse response = cartService.removeItem(authentication.getName(), itemId);
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .code(0)
                .message("Item removed")
                .data(response)
                .build());
    }

    @DeleteMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Clear cart")
    public ResponseEntity<ApiResponse<Void>> clearCart(Authentication authentication) {
        cartService.clearCart(authentication.getName());
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .code(0)
                .message("Cart cleared")
                .build());
    }
}
