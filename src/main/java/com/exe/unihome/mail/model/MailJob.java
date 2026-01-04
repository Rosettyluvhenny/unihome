package com.exe.unihome.mail.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailJob {

  private String jobId;          // UUID
  private String type;           // VERIFY_EMAIL
  private String toEmail;
  private String fullName;
  private String verifyToken;
  private int retryCount;
  private Instant createdAt;
}
