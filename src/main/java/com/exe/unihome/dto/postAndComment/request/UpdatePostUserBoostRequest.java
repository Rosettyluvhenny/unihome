package com.exe.unihome.dto.postAndComment.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePostUserBoostRequest {
  private LocalDateTime endTime;
}

