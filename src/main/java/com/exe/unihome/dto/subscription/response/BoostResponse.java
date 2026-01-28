package com.exe.unihome.dto.subscription.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoostResponse {

  private String id;

  private String name;

  private BigDecimal price;

  private Integer duration;

  private String status;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}

