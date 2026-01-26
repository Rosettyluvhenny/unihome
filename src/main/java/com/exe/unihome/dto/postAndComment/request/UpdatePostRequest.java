package com.exe.unihome.dto.postAndComment.request;

import com.exe.unihome.persistence.entity.postAndComment.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostRequest {

  private String title;

  private String price;

  private PostStatus status;

  private UUID categoryId;
}

