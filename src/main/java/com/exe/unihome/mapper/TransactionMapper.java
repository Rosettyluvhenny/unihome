package com.exe.unihome.mapper;

import com.exe.unihome.dto.payment.response.TransactionResponse;
import com.exe.unihome.persistence.entity.payment.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

  @Mapping(source = "order.orderId", target = "orderId")
  @Mapping(source = "payment.id", target = "paymentId")
  TransactionResponse toResponse(Transaction transaction);

  Transaction toEntity(TransactionResponse response);
}

