package com.ask.service;

/**
 * Email service interface for sending emails.
 * Implemented via JavaMailSender + Gmail SMTP.
 * Behind an interface so providers can be swapped later.
 */
public interface EmailService {

    /**
     * Sends a simple text email.
     *
     * @param to      recipient email address
     * @param subject email subject
     * @param body    email body text
     */
    void sendSimpleEmail(String to, String subject, String body);

    /**
     * Sends a 2FA OTP email to the user.
     *
     * @param to       recipient email address
     * @param userName the user's name for personalisation
     * @param otp      the OTP code
     */
    void sendOtpEmail(String to, String userName, String otp);
}
