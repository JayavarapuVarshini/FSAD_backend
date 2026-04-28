package com.edufeedback.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Async
    public void sendOtpEmail(String toEmail, String otp, String purpose) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);

            String subject;
            String htmlContent;

            if ("EMAIL_VERIFICATION".equals(purpose)) {
                subject = "EduFeedback - Email Verification OTP";
                htmlContent = buildVerificationEmail(otp);
            } else {
                subject = "EduFeedback - Password Reset OTP";
                htmlContent = buildPasswordResetEmail(otp);
            }

            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);

        } catch (Exception e) {
            // Log but do NOT rethrow — email failure must not crash registration/OTP flow.
            // The register() method already handles email failure gracefully.
            System.err.println("[EmailService] Failed to send OTP email to " + toEmail + ": " + e.getMessage());
        }
    }

    private String buildVerificationEmail(String otp) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8"/>
              <style>
                body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 0; }
                .container { max-width: 520px; margin: 40px auto; background: #fff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
                .header { background: linear-gradient(135deg, #1E3A8A, #3B82F6); padding: 30px; text-align: center; }
                .header h1 { color: #fff; margin: 0; font-size: 28px; letter-spacing: 1px; }
                .header p { color: #bcd4fa; margin: 5px 0 0 0; font-size: 14px; }
                .body { padding: 35px 40px; text-align: center; }
                .body p { color: #444; font-size: 15px; line-height: 1.6; }
                .otp-box { display: inline-block; background: #EFF6FF; border: 2px dashed #3B82F6; border-radius: 12px; padding: 18px 40px; margin: 20px 0; }
                .otp { font-size: 42px; font-weight: bold; letter-spacing: 10px; color: #1E3A8A; }
                .note { font-size: 13px; color: #888; margin-top: 15px; }
                .footer { background: #f9f9f9; padding: 18px; text-align: center; font-size: 12px; color: #aaa; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>🎓 EduFeedback</h1>
                  <p>Your trusted academic feedback platform</p>
                </div>
                <div class="body">
                  <p>Hello! You requested email verification for your EduFeedback account.</p>
                  <p>Your One-Time Password (OTP) is:</p>
                  <div class="otp-box">
                    <div class="otp">%s</div>
                  </div>
                  <p class="note">⏰ This OTP is valid for <strong>10 minutes</strong>. Do not share it with anyone.</p>
                </div>
                <div class="footer">
                  &copy; 2024 EduFeedback &bull; If you did not request this, please ignore this email.
                </div>
              </div>
            </body>
            </html>
            """.formatted(otp);
    }

    private String buildPasswordResetEmail(String otp) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
              <meta charset="UTF-8"/>
              <style>
                body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; padding: 0; }
                .container { max-width: 520px; margin: 40px auto; background: #fff; border-radius: 10px; overflow: hidden; box-shadow: 0 4px 15px rgba(0,0,0,0.1); }
                .header { background: linear-gradient(135deg, #7C3AED, #A78BFA); padding: 30px; text-align: center; }
                .header h1 { color: #fff; margin: 0; font-size: 28px; }
                .body { padding: 35px 40px; text-align: center; }
                .otp-box { display: inline-block; background: #F5F3FF; border: 2px dashed #7C3AED; border-radius: 12px; padding: 18px 40px; margin: 20px 0; }
                .otp { font-size: 42px; font-weight: bold; letter-spacing: 10px; color: #7C3AED; }
                .note { font-size: 13px; color: #888; margin-top: 15px; }
                .footer { background: #f9f9f9; padding: 18px; text-align: center; font-size: 12px; color: #aaa; }
              </style>
            </head>
            <body>
              <div class="container">
                <div class="header">
                  <h1>🔐 Password Reset</h1>
                </div>
                <div class="body">
                  <p>You requested a password reset for your EduFeedback account.</p>
                  <div class="otp-box">
                    <div class="otp">%s</div>
                  </div>
                  <p class="note">⏰ This OTP expires in <strong>10 minutes</strong>.</p>
                </div>
                <div class="footer">&copy; 2024 EduFeedback</div>
              </div>
            </body>
            </html>
            """.formatted(otp);
    }
}
