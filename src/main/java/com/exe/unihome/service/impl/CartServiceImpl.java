package com.exe.unihome.service.impl;

import com.exe.unihome.common.exception.AppException;
import com.exe.unihome.common.exception.ErrorCode;
import com.exe.unihome.dto.cart.request.CartItemRequest;
import com.exe.unihome.dto.cart.request.UpdateCartItemRequest;
import com.exe.unihome.dto.cart.response.CartResponse;
import com.exe.unihome.mapper.CartMapper;
import com.exe.unihome.persistence.entity.Furniture;
import com.exe.unihome.persistence.entity.FurnitureSku;
import com.exe.unihome.persistence.entity.cart.Cart;
import com.exe.unihome.persistence.entity.cart.CartItem;
import com.exe.unihome.persistence.entity.identityAndAuth.User;
import com.exe.unihome.persistence.repository.CartRepository;
import com.exe.unihome.persistence.repository.FurnitureRepository;
import com.exe.unihome.persistence.repository.FurnitureSkuRepository;
import com.exe.unihome.persistence.repository.UserRepository;
import com.exe.unihome.service.CartService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final UserRepository userRepository;
    private final FurnitureRepository furnitureRepository;
    private final FurnitureSkuRepository skuRepository;
    private final CartMapper cartMapper;

    @Override
    @Transactional
    public CartResponse getCart(String userId) {
        Cart cart = cartRepository.findWithItemsByUserId(userId)
                .orElseGet(() -> createCart(userId));
        return cartMapper.toResponse(cart);
    }

    @Override
    @Transactional
    public CartResponse addItem(String userId, CartItemRequest request) {
        Cart cart = getOrCreateCartForUpdate(userId);

        Furniture furniture = furnitureRepository.findById(request.getFurnitureId())
                .orElseThrow(() -> new AppException(ErrorCode.FURNITURE_NOT_FOUND));

        FurnitureSku sku = skuRepository.findBySkuId(request.getSkuId())
                .orElseThrow(() -> new AppException(ErrorCode.SKU_NOT_FOUND));

        // Validate SKU belongs to the requested furniture
        if (!sku.getFurniture().getFurnitureId().equals(furniture.getFurnitureId())) {
            throw new AppException(ErrorCode.INVALID_REQUEST);
        }

        CartItem item = findItemBySku(cart, sku.getSkuId())
                .orElseGet(() -> {
                    CartItem newItem = CartItem.builder()
                            .cart(cart)
                            .furniture(furniture)
                            .sku(sku)
                            .quantity(0)
                            .unitPrice(resolveUnitPrice(sku))
                            .lineTotal(BigDecimal.ZERO)
                            .build();
                    cart.getItems().add(newItem);
                    return newItem;
                });

        int newQuantity = item.getQuantity() + request.getQuantity();
        item.setQuantity(newQuantity);
        item.setUnitPrice(resolveUnitPrice(sku));
        item.setLineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

        recalculateTotals(cart);
        Cart persisted = cartRepository.save(cart);
        return cartMapper.toResponse(persisted);
    }

    @Override
    @Transactional
    public CartResponse updateItem(String userId, UUID cartItemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCartForUpdate(userId);
        CartItem item = cart.getItems().stream()
                .filter(ci -> Objects.equals(ci.getCartItemId(), cartItemId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));

        item.setQuantity(request.getQuantity());
        item.setLineTotal(item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())));

        recalculateTotals(cart);
        Cart persisted = cartRepository.save(cart);
        return cartMapper.toResponse(persisted);
    }

    @Override
    @Transactional
    public CartResponse removeItem(String userId, UUID cartItemId) {
        Cart cart = getOrCreateCartForUpdate(userId);
        boolean removed = cart.getItems().removeIf(ci -> Objects.equals(ci.getCartItemId(), cartItemId));
        if (!removed) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
        recalculateTotals(cart);
        Cart persisted = cartRepository.save(cart);
        return cartMapper.toResponse(persisted);
    }

    @Override
    @Transactional
    public void clearCart(String userId) {
        Cart cart = getOrCreateCartForUpdate(userId);
        cart.getItems().clear();
        cart.setTotalAmount(BigDecimal.ZERO);
        cart.setTotalQuantity(0);
        cartRepository.save(cart);
    }

    private Cart getOrCreateCartForUpdate(String userId) {
        Optional<Cart> cartOpt = cartRepository.findForUpdateByUserId(userId);
        return cartOpt.orElseGet(() -> createCart(userId));
    }

    private Cart createCart(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        Cart cart = Cart.builder()
                .user(user)
                .build();
        return cartRepository.save(cart);
    }

    private Optional<CartItem> findItemBySku(Cart cart, UUID skuId) {
        return cart.getItems().stream()
                .filter(ci -> ci.getSku() != null && Objects.equals(ci.getSku().getSkuId(), skuId))
                .findFirst();
    }

    private void recalculateTotals(Cart cart) {
        int totalQuantity = cart.getItems().stream()
                .mapToInt(CartItem::getQuantity)
                .sum();
        BigDecimal totalAmount = cart.getItems().stream()
                .map(CartItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalQuantity(totalQuantity);
        cart.setTotalAmount(totalAmount);
    }

    private BigDecimal resolveUnitPrice(FurnitureSku sku) {
        return sku.getFinalPrice() != null ? sku.getFinalPrice() : sku.getPrice();
    }
}
