package com.exe.unihome.dto.subscription.request;

import com.exe.unihome.persistence.entity.subscription.UserBoostStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserBoostRequest {
  @NotNull
  private UserBoostStatus status;
}

