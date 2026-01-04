package com.exe.unihome.mail.service;


public interface MailService {
  void sendTestMail(String to);

  void sendVerificationEmail(String to, String fullName, String verifyToken);
}
