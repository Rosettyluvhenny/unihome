package com.exe.unihome.controller;

import com.exe.unihome.common.model.ApiResponse;
import com.exe.unihome.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
public class MailController {
  private final MailService mailService;

  @GetMapping
  public ResponseEntity<ApiResponse<Void>> sendTestMail(
    @RequestParam String to
  ) {
    mailService.sendTestMail(to);
    return ResponseEntity.ok(ApiResponse.<Void>builder()
      .code(200)
      .message("Test mail sent successfully to " + to)
      .build());
  }

}
