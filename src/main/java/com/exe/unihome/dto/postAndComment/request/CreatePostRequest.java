package com.exe.unihome.dto.postAndComment.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostRequest {

  @NotBlank(message = "Title is required")
  private String title;

  @NotNull(message = "Price is required")
  @Positive(message = "Price must be positive")
  private BigDecimal price;

  @NotNull(message = "Category ID is required")
  private UUID categoryId;

  @NotNull(message = "Status is required")
  private String status;

  @Valid
  private CreatePostDetailRequest postDetail;
}

