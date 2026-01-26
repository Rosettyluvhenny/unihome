package com.exe.unihome.dto.subscription.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserBoostRequest {

  @NotBlank(message = "User ID is required")
  private String userId;

  @NotBlank(message = "Boost Id is required")
  private String boostId;
}

