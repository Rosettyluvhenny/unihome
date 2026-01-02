package com.exe.unihome.controller;

import com.exe.unihome.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailSender;
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
  public void  sendTestMail(
    @RequestParam String to
  ) {
    mailService.sendTestMail(to);
  }

}
