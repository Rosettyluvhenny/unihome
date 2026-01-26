package com.exe.unihome.dto.subscription.request;

import com.exe.unihome.persistence.entity.subscription.BoostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBoostRequest {

  @NotBlank(message = "Boost name is required")
  private String name;

  @NotNull(message = "Price is required")
  private BigDecimal price;

  @NotNull(message = "Duration is required")
  private Integer duration = 0;

  private BoostStatus status;
}

