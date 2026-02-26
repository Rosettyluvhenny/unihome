package com.exe.unihome.dto.payment.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Cancel Transaction Request DTO
 * Used to cancel an existing transaction
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CancelTransactionRequest {

  private String reason;
}

