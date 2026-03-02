package com.exe.unihome.dto.payment.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {

  @NotBlank(message = "Payment ID is required")
  private String id;

  @NotBlank(message = "Payment name is required")
  private String name;

  private String img;
}

