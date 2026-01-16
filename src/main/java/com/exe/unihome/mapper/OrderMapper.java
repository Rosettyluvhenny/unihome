package com.exe.unihome.mapper;

import com.exe.unihome.dto.order.response.OrderResponse;
import com.exe.unihome.persistence.entity.order.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {OrderItemMapper.class})
public interface OrderMapper {

    @Mapping(source = "user.id", target = "userId")
    OrderResponse toResponse(Order order);
}
