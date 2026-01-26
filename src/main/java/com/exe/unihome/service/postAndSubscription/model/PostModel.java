package com.exe.unihome.service.postAndSubscription.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostModel {

  private String id;

  private String title;

  private BigDecimal price;

  private String status;

  private String categoryId;

  private String categoryName;

  private String userId;

  private String userName;

  private PostDetailModel postDetail;

  private List<CommentModel> comments;

  private List<PostUserBoostModel> postUserBoosts;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}

