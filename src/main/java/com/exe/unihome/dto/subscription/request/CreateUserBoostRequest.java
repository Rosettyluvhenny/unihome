package com.exe.unihome.dto.subscription.request;

import com.exe.unihome.persistence.entity.subscription.UserBoostStatus;
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
public class CreateUserBoostRequest {

  @NotBlank(message = "User ID is required")
  private String userId;

  @NotNull(message = "Price is required")
  private BigDecimal price;

  private UserBoostStatus status;
}

