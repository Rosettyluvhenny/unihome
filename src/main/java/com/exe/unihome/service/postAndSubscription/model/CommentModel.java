package com.exe.unihome.service.postAndSubscription.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentModel {

  private String id;

  private String content;

  private String userId;

  private String userName;

  private String postId;

  private String replyId;

  private List<CommentModel> replies;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}

