package com.ask.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Two-factor authentication configuration per user.
 * Mandatory for SUPER_ADMIN and SYSTEM_ADMIN.
 * Optional for STATE_ADMIN and DISTRICT_ADMIN.
 * Not available for store-level roles.
 */
@Entity
@Table(name = "two_factor_config")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TwoFactorConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "is_enabled", nullable = false)
    @Builder.Default
    private Boolean isEnabled = false;

    @Column(name = "is_mandatory", nullable = false)
    @Builder.Default
    private Boolean isMandatory = false;

    @Column(name = "otp_code")
    private String otpCode;

    @Column(name = "otp_expires_at")
    private LocalDateTime otpExpiresAt;

    @Column(name = "pending_login_token_hash")
    private String pendingLoginTokenHash;

    @Column(name = "pending_login_expires_at")
    private LocalDateTime pendingLoginExpiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Checks if the stored OTP is still valid (not expired).
     * @return true if OTP exists and has not expired
     */
    public boolean isOtpValid() {
        return otpCode != null
                && otpExpiresAt != null
                && LocalDateTime.now().isBefore(otpExpiresAt);
    }

    /**
     * Checks if a password-authenticated 2FA login challenge is still valid.
     * @return true if the pending challenge exists and has not expired
     */
    public boolean isPendingLoginValid() {
        return pendingLoginTokenHash != null
                && pendingLoginExpiresAt != null
                && LocalDateTime.now().isBefore(pendingLoginExpiresAt);
    }
}
