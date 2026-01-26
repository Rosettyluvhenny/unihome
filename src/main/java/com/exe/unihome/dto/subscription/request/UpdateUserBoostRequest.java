package com.exe.unihome.dto.subscription.request;

import com.exe.unihome.persistence.entity.subscription.UserBoostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserBoostRequest {

  private BigDecimal price;

  private UserBoostStatus status;
}

