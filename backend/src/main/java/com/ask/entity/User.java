package com.ask.entity;

import com.ask.enums.Gender;
import com.ask.enums.UserStatus;
import com.ask.enums.VerificationStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Core user entity. Every person in the system (admin, receptionist, pharmacist, etc.).
 * Each user has a role and geographic assignment that determines their data scope.
 */
@Entity
@Table(name = "users")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 150)
    private String fullName;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(unique = true, length = 15)
    private String phone;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(columnDefinition = "TEXT")
    private String address;

    // --- Role & Geographic Assignment ---

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "state_id")
    private State state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "block_id")
    private Block block;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_id")
    private Store store;

    // --- Sensitive Fields (encrypted/masked) ---

    @Column(name = "bank_account_encrypted", columnDefinition = "TEXT")
    private String bankAccountEncrypted;

    @Column(name = "bank_ifsc", length = 20)
    private String bankIfsc;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "pan_number", length = 10)
    private String panNumber;

    @Column(name = "aadhaar_last_four", length = 4)
    private String aadhaarLastFour;

    @Column(name = "aadhaar_doc_url", length = 500)
    private String aadhaarDocUrl;

    // --- Status & Security ---

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_status", nullable = false)
    @Builder.Default
    private VerificationStatus verificationStatus = VerificationStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "failed_login_attempts", nullable = false)
    @Builder.Default
    private Integer failedLoginAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @Column(name = "force_password_change", nullable = false)
    @Builder.Default
    private Boolean forcePasswordChange = false;

    @Column(name = "password_changed_at")
    private LocalDateTime passwordChangedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    // --- Audit ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Checks if the account is currently locked.
     * @return true if locked and lock has not expired
     */
    public boolean isAccountLocked() {
        return status == UserStatus.LOCKED
                && lockedUntil != null
                && LocalDateTime.now().isBefore(lockedUntil);
    }

    /**
     * Checks if the account is active and not locked.
     * @return true if the user can authenticate
     */
    public boolean isAccountActive() {
        return status == UserStatus.ACTIVE;
    }
}
