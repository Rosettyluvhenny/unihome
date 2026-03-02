package com.exe.unihome.dto.payment.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserBoostTransactionRequest {
  private String userBoostId;
  private String paymentMethodId;
}



