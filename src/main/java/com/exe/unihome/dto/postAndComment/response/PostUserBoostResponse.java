package com.exe.unihome.dto.postAndComment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostUserBoostResponse {

  private String id;

  private String userBoostId;

  private String postId;

  private LocalDateTime startTime;

  private LocalDateTime endTime;

  private String status;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}

