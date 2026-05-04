package com.ask.service.impl;

import com.ask.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Email service implementation using Brevo (formerly Sendinblue) HTTP API.
 * Sends emails over HTTPS (port 443) instead of SMTP (port 587/465),
 * which avoids firewall blocks on cloud platforms like Railway.
 *
 * Requires BREVO_API_KEY and BREVO_SENDER_EMAIL environment variables.
 */
@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private static final String BREVO_API_URL = "https://api.brevo.com/v3/smtp/email";

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String senderEmail;
    private final String senderName;

    public EmailServiceImpl(
            @Value("${ask.brevo.api-key}") String apiKey,
            @Value("${ask.brevo.sender-email}") String senderEmail,
            @Value("${ask.brevo.sender-name:Apna Swasthya Kendra}") String senderName) {
        this.restTemplate = new RestTemplate();
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
    }

    @Async
    @Override
    public void sendSimpleEmail(String to, String subject, String body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("api-key", apiKey);

            Map<String, Object> payload = Map.of(
                    "sender", Map.of("name", senderName, "email", senderEmail),
                    "to", List.of(Map.of("email", to)),
                    "subject", subject,
                    "textContent", body);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    BREVO_API_URL, HttpMethod.POST, request, String.class);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Email sent to: {}, subject: {}", to, subject);
            } else {
                log.error("Brevo API returned non-2xx status: {} - {}",
                        response.getStatusCode(), response.getBody());
            }
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
                        "Regards,\nApna Swasthya Kendra",
                userName, otp);
        sendSimpleEmail(to, subject, body);
    }
}
