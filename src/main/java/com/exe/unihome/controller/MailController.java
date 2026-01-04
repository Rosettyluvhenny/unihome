package com.exe.unihome.controller;

import com.exe.unihome.mail.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
  @PreAuthorize("hasRole('CUSTOMER')")
  public void sendTestMail(
    @RequestParam String to
  ) {
    mailService.sendTestMail(to);
  }

}
