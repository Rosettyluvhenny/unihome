package com.exe.unihome.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePaymentLinkRequestBody {
  private String productName;
  private String description;
  private String returnUrl;
  private int price;
  private String cancelUrl;
  private long expiredAt;
}
