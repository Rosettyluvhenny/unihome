package com.exe.unihome.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentLinkRequest {
  private String productName;
  private String description;
  private String returnUrl;
  private int price;
  private String cancelUrl;

}
