package com.exe.unihome.dto.payment.response;

import com.exe.unihome.persistence.enums.TransactionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Transaction Response DTO
 * Returns transaction details to the client
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {

  private String id;
  private String orderId;
  private String paymentId;
  private TransactionStatus status;
  private String userBoostId;
  private String url;
  private String payOsCode;
  private String payOsQr;
  private LocalDateTime paidAt;
  private LocalDateTime expiredAt;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
}

