package com.exe.unihome.service.postAndSubscription.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailModel {

  private String id;

  private String description;

  private String image;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}

