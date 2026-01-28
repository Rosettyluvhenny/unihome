package com.exe.unihome.dto.postAndComment.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponse {

  private String id;

  private String title;

  private BigDecimal price;

  private String status;

  private UUID categoryId;

  private String categoryName;

  private String userId;

  private String userName;

  private String userImage;

  private List<PostDetailResponse> postDetail;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;

  private boolean isBoost;
}

