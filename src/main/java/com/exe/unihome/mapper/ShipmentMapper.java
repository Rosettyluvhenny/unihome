package com.exe.unihome.mapper;

import com.exe.unihome.dto.shipment.response.ShipmentResponse;
import com.exe.unihome.persistence.entity.shipment.Shipment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShipmentMapper {

    @Mapping(source = "transaction.id", target = "transactionId")
    @Mapping(source = "transaction.order.orderId", target = "orderId")
    @Mapping(source = "shipper.id", target = "shipperId")
    @Mapping(source = "shipper.fullName", target = "shipperName")
    ShipmentResponse toResponse(Shipment shipment);
}
