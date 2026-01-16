package com.exe.unihome.service;

import com.exe.unihome.dto.cart.request.CartItemRequest;
import com.exe.unihome.dto.cart.request.UpdateCartItemRequest;
import com.exe.unihome.dto.cart.response.CartResponse;

import java.util.UUID;

public interface CartService {

    CartResponse getCart(String userId);

    CartResponse addItem(String userId, CartItemRequest request);

    CartResponse updateItem(String userId, UUID cartItemId, UpdateCartItemRequest request);

    CartResponse removeItem(String userId, UUID cartItemId);

    void clearCart(String userId);
}
