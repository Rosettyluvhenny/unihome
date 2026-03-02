package com.exe.unihome.dto.payment.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create Transaction Request DTO
 * Used to create a new payment transaction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTransactionRequest {

  private String orderId;
  private String paymentMethodId;
  private String paymentUrl;
}

