package com.exe.unihome.mapper;

import com.exe.unihome.dto.payment.request.CreatePaymentRequest;
import com.exe.unihome.dto.payment.response.PaymentResponse;
import com.exe.unihome.persistence.entity.payment.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {

  PaymentResponse toResponse(Payment payment);

  Payment toEntity(CreatePaymentRequest request);
}

