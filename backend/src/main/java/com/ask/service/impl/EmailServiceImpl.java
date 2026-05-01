package com.ask.service.impl;

import com.ask.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Email service implementation using JavaMailSender with Gmail SMTP.
 * All email sending is async so it never blocks API responses.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("Email sent to: {}, subject: {}", to, subject);
        } catch (Exception e) {
            // Email failure should not crash the application
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    @Async
    @Override
    public void sendOtpEmail(String to, String userName, String otp) {
        String subject = "ASK - Your Verification Code";
        String body = String.format(
                "Dear %s,\n\n" +
                "Your one-time verification code is: %s\n\n" +
                "This code will expire in 5 minutes.\n" +
                "If you did not request this code, please ignore this email.\n\n" +
                "Regards,\nApna Swasthya Kendra", userName, otp);
        sendSimpleEmail(to, subject, body);
    }
}
