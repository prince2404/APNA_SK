package com.ask.config;

import com.ask.constants.RoleConstants;
import com.ask.entity.Role;
import com.ask.entity.TwoFactorConfig;
import com.ask.entity.User;
import com.ask.enums.UserStatus;
import com.ask.enums.VerificationStatus;
import com.ask.repository.RoleRepository;
import com.ask.repository.TwoFactorConfigRepository;
import com.ask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the Super Admin account on first startup if one doesn't already exist.
 * Reads credentials from environment variables (SUPER_ADMIN_EMAIL, SUPER_ADMIN_PASSWORD).
 * This avoids hardcoding admin credentials in SQL migrations.
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class SuperAdminSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TwoFactorConfigRepository twoFactorConfigRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ask.admin.email:}")
    private String adminEmail;

    @Value("${ask.admin.password:}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        // Skip if env vars not configured
        if (adminEmail == null || adminEmail.isBlank() || adminPassword == null || adminPassword.isBlank()) {
            log.warn("SUPER_ADMIN_EMAIL or SUPER_ADMIN_PASSWORD not set — skipping Super Admin creation.");
            return;
        }

        // Skip if a Super Admin already exists
        Role superAdminRole = roleRepository.findByName(RoleConstants.SUPER_ADMIN).orElse(null);
        if (superAdminRole == null) {
            log.warn("SUPER_ADMIN role not found in database — skipping Super Admin creation.");
            return;
        }

        boolean adminExists = userRepository.findByEmail(adminEmail).isPresent();
        if (adminExists) {
            log.info("Super Admin account already exists ({}). Skipping.", adminEmail);
            return;
        }

        // Create Super Admin
        User admin = User.builder()
                .fullName("Super Admin")
                .email(adminEmail.toLowerCase())
                .phone("0000000000")
                .passwordHash(passwordEncoder.encode(adminPassword))
                .role(superAdminRole)
                .status(UserStatus.ACTIVE)
                .verificationStatus(VerificationStatus.VERIFIED)
                .forcePasswordChange(false)
                .build();

        User saved = userRepository.save(admin);

        // Create mandatory 2FA config
        twoFactorConfigRepository.save(TwoFactorConfig.builder()
                .user(saved)
                .isEnabled(true)
                .isMandatory(true)
                .build());

        log.info("✅ Super Admin account created successfully: {}", adminEmail);
    }
}
