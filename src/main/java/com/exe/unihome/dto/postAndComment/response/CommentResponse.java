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
public class CommentResponse {

  private String id;

  private String content;

  private String userId;

  private String userName;

  private String userImage;

  private String postId;

  private String replyId;

  private LocalDateTime createdAt;

  private LocalDateTime updatedAt;
}

