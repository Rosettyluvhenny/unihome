package com.exe.unihome.service;


public interface MailService {
  void sendTestMail(String to);

  void sendVerificationEmail(String to, String fullName, String verifyToken);
}
