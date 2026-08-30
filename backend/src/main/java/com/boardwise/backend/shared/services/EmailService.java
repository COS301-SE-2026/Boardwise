package com.boardwise.backend.shared.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender mailSender;

    @Value("${frontend.base.url}")
    private String frontendBaseUrl;

    @Async
    public void sendPasswordResetEmail(String recipient, String resetToken){
        String resetUrl = frontendBaseUrl + "auth/reset?token=" + resetToken;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("no-reply@boardwise.games");
        message.setTo(recipient);
        message.setSubject("Password Reset Request");
        message.setText(
            "To complete the reset password request, click the link below to set a new password:\n\n"
            + resetUrl
            + "\n\nThis link will expire in 15 minutes. If you did not request this, please ignore this email.");
        
        try{
            mailSender.send(message);
        }catch(Exception e){
            log.error("Failed to send password reset email to " + recipient + ": " + e.getMessage());
        }
    }
}
