package com.exe.unihome.dto.postAndComment.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePostUserBoostRequest {

  @NotNull(message = "User Boost ID is required")
  private String userBoostId;

  @NotNull(message = "Post ID is required")
  private String postId;

  @NotNull(message = "Start time is required")
  private LocalDateTime startTime;

  @NotNull(message = "End time is required")
  private LocalDateTime endTime;

  private String status;
}

