package com.exe.unihome.dto.subscription.request;

import com.exe.unihome.persistence.entity.subscription.BoostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBoostRequest {

  private String name;

  private BigDecimal price;

  private Integer duration;

  private BoostStatus status;
}

