package com.exe.unihome.service.impl;

import com.exe.unihome.service.MailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {
  private final JavaMailSender mailSender;

  @Override
  public void sendTestMail(String to) {
    SimpleMailMessage msg = new SimpleMailMessage();
    msg.setTo(to);
    msg.setSubject("Test mail");
    msg.setText("Hello SMTP");

    mailSender.send(msg);
  }

  @Override
  public void sendVerificationEmail(String to, String fullName, String verifyToken) {
    try {
      MimeMessage mimeMessage = mailSender.createMimeMessage();
      MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

      helper.setTo(to);
      helper.setSubject("Email Verification - UNIHOME");
      helper.setText(buildVerificationEmailHtml(fullName, verifyToken), true);

      mailSender.send(mimeMessage);
    } catch (MessagingException e) {
      throw new RuntimeException("Failed to send verification email", e);
    }
  }

  private String buildVerificationEmailHtml(String fullName, String verifyToken) {
    String verificationUrl = "http://localhost:8080/unihome/auth/verify?token=" + verifyToken;

    return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Email Verification - UNIHOME</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
                        background-color: #f5f5f5;
                        line-height: 1.6;
                        color: #333;
                    }
                    .email-container {
                        max-width: 600px;
                        margin: 0 auto;
                        background-color: #ffffff;
                        border-radius: 8px;
                        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: #2ec492;
                        color: white;
                        padding: 40px 20px;
                        text-align: center;
                    }
                    .header h1 {
                        font-size: 32px;
                        font-weight: 700;
                        margin: 0;
                        letter-spacing: 1px;
                    }
                    .content {
                        padding: 40px 30px;
                    }
                    .greeting {
                        font-size: 18px;
                        color: #333;
                        margin-bottom: 20px;
                    }
                    .greeting strong {
                        color: #667eea;
                    }
                    .message {
                        font-size: 16px;
                        color: #666;
                        margin-bottom: 30px;
                        line-height: 1.8;
                    }
                    .verification-section {
                        background-color: #f9f9f9;
                        border-left: 4px solid #2EC4B6;
                        padding: 20px;
                        margin: 30px 0;
                        border-radius: 4px;
                    }
                    .verification-section p {
                        font-size: 14px;
                        color: #666;
                        margin-bottom: 15px;
                    }
                    .button-container {
                        text-align: center;
                        margin: 30px 0;
                    }
                    .verify-button {
                        display: inline-block;
                      background: #2ec492;
                        color: white;
                        padding: 14px 40px;
                        border-radius: 6px;
                        text-decoration: none;
                        font-weight: 600;
                        font-size: 16px;
                        transition: transform 0.2s, box-shadow 0.2s;
                        box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
                    }
                    .verify-button:hover {
                        transform: translateY(-2px);
                        box-shadow: 0 6px 20px rgba(102, 126, 234, 0.6);
                    }
                    .token-code {
                        background-color: #f0f0f0;
                        border: 1px solid #ddd;
                        padding: 15px;
                        border-radius: 4px;
                        font-family: 'Courier New', monospace;
                        font-size: 12px;
                        word-break: break-all;
                        color: #333;
                        margin: 15px 0;
                    }
                    .footer {
                        background-color: #f5f5f5;
                        padding: 20px 30px;
                        text-align: center;
                        border-top: 1px solid #eee;
                        font-size: 12px;
                        color: #999;
                    }
                    .footer a {
                        color: #667eea;
                        text-decoration: none;
                    }
                    .expiry-notice {
                        background-color: #fff3cd;
                        border-left: 4px solid #ffc107;
                        padding: 12px 15px;
                        border-radius: 4px;
                        font-size: 13px;
                        color: #856404;
                        margin-top: 20px;
                    }
                </style>
            </head>
            <body>
                <div class="email-container">
                    <!-- Header -->
                    <div class="header">
                        <h1>UNIHOME</h1>
                    </div>

                    <!-- Content -->
                    <div class="content">
                        <div class="greeting">
                            Welcome, <strong>%s</strong>! 👋
                        </div>

                        <div class="message">
                            Thank you for registering with UNIHOME. To complete your account setup and activate your access, please verify your email address by clicking the button below.
                        </div>

                        <!-- Verification Button -->
                        <div class="button-container">
                            <a href="%s" class="verify-button">Verify Email Address</a>
                        </div>

                        <!-- Additional Information -->
                        <div class="message">
                            <strong>What happens next?</strong><br>
                            Once you verify your email, your account will be activated and you can start using UNIHOME immediately. If you didn't create this account, you can safely ignore this email.
                        </div>

                        <!-- Expiry Notice -->
                        <div class="expiry-notice">
                            ⏰ This verification link will expire in 24 hours. If it expires, you can request a new verification email from your account settings.
                        </div>
                    </div>

                    <!-- Footer -->
                    <div class="footer">
                        <p>© 2026 UNIHOME. All rights reserved. | <a href="https://unihome.local">Visit Website</a></p>
                        <p>If you have any questions, contact our support team.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(fullName, verificationUrl);
  }
}

