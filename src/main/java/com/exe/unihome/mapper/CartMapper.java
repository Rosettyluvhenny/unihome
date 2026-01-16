package com.exe.unihome.mapper;

import com.exe.unihome.dto.cart.response.CartResponse;
import com.exe.unihome.persistence.entity.cart.Cart;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {CartItemMapper.class})
public interface CartMapper {

    @Mapping(source = "user.id", target = "userId")
    CartResponse toResponse(Cart cart);
}
