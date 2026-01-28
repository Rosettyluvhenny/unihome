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
public class PostDetailResponse {

  private String id;

  private String description;

  private String image;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}

