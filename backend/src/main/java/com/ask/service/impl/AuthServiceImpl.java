package com.ask.service.impl;

import com.ask.constants.ErrorMessages;
import com.ask.dto.request.auth.*;
import com.ask.dto.response.auth.LoginResponse;
import com.ask.entity.*;
import com.ask.enums.UserStatus;
import com.ask.exception.*;
import com.ask.repository.*;
import com.ask.security.JwtTokenProvider;
import com.ask.service.AuditService;
import com.ask.service.AuthService;
import com.ask.service.EmailService;
import com.ask.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/**
 * Authentication service implementation.
 * Handles: login, 2FA OTP flow, token refresh, logout, password change, account
 * lockout.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserSessionRepository userSessionRepository;
    private final TwoFactorConfigRepository twoFactorConfigRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final AuditService auditService;
    private final EmailService emailService;

    @Value("${ask.security.max-failed-attempts}")
    private int maxFailedAttempts;

    @Value("${ask.security.lockout-duration-minutes}")
    private int lockoutDurationMinutes;

    @Value("${ask.otp.expiry-minutes}")
    private int otpExpiryMinutes;

    @Value("${ask.otp.length}")
    private int otpLength;

    /**
     * Authenticates user with email + password.
     * Handles: account lockout, 2FA check, forced password change, session
     * creation.
     */
    @Override
    @Transactional
    public LoginResponse login(LoginRequest request, String ipAddress, String userAgent) {
        // Step 1: Find user by email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException(ErrorMessages.INVALID_CREDENTIALS));

        // Step 2: Check if account is inactive
        if (user.getStatus() == UserStatus.INACTIVE) {
            throw new InvalidRequestException(ErrorMessages.ACCOUNT_INACTIVE);
        }

        // Step 3: Check if account is locked
        if (user.isAccountLocked()) {
            throw new AccountLockedException(
                    String.format(ErrorMessages.ACCOUNT_LOCKED, lockoutDurationMinutes));
        }

        // Step 4: If lock has expired, reset the lock
        if (user.getStatus() == UserStatus.LOCKED && !user.isAccountLocked()) {
            user.setStatus(UserStatus.ACTIVE);
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);
        }

        // Step 5: Authenticate with Spring Security
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

            // Step 6: Reset failed attempts on successful auth
            user.setFailedLoginAttempts(0);
            user.setLastLoginAt(LocalDateTime.now());
            userRepository.save(user);

            // Step 7: Check if 2FA is required
            var twoFactorConfig = twoFactorConfigRepository.findByUserId(user.getId());
            if (twoFactorConfig.isPresent()
                    && (twoFactorConfig.get().getIsEnabled() || twoFactorConfig.get().getIsMandatory())) {
                // Generate and send OTP, return partial response
                String challengeToken = createPendingLoginChallenge(twoFactorConfig.get());
                generateAndSendOtp(user, twoFactorConfig.get());
                auditService.log(user, "LOGIN_2FA_PENDING", ipAddress, "Login successful, awaiting 2FA OTP");

                return LoginResponse.builder()
                        .requiresTwoFactor(true)
                        .requiresPasswordChange(user.getForcePasswordChange())
                        .twoFactorChallengeToken(challengeToken)
                        .user(buildUserProfile(user))
                        .build();
            }

            // Step 8: Check forced password change
            if (user.getForcePasswordChange()) {
                // Generate tokens but flag password change required
                String accessToken = jwtTokenProvider.generateAccessToken(authentication);
                return LoginResponse.builder()
                        .accessToken(accessToken)
                        .tokenType("Bearer")
                        .requiresTwoFactor(false)
                        .requiresPasswordChange(true)
                        .user(buildUserProfile(user))
                        .build();
            }

            // Step 9: Full login — generate tokens and create session
            return completeLogin(user, ipAddress, userAgent);

        } catch (BadCredentialsException e) {
            // Increment failed attempts
            handleFailedLogin(user, ipAddress);
            throw e;
        }
    }

    /**
     * Verifies 2FA OTP and completes login.
     */
    @Override
    @Transactional
    public LoginResponse verifyOtp(VerifyOtpRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidRequestException(ErrorMessages.ACCOUNT_INACTIVE);
        }

        TwoFactorConfig config = twoFactorConfigRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidRequestException("2FA is not configured for this user"));

        validatePendingLoginChallenge(config, request.getChallengeToken());

        // Validate OTP
        if (!config.isOtpValid()) {
            throw new InvalidRequestException(ErrorMessages.OTP_EXPIRED);
        }

        if (!passwordEncoder.matches(request.getOtp(), config.getOtpCode())) {
            throw new InvalidRequestException(ErrorMessages.OTP_INVALID);
        }

        // Clear OTP after successful verification
        config.setOtpCode(null);
        config.setOtpExpiresAt(null);
        config.setPendingLoginTokenHash(null);
        config.setPendingLoginExpiresAt(null);
        twoFactorConfigRepository.save(config);

        auditService.log(user, "LOGIN_2FA_VERIFIED", ipAddress, "2FA OTP verified successfully");

        // Check forced password change
        if (user.getForcePasswordChange()) {
            String accessToken = jwtTokenProvider.generateAccessTokenFromEmail(user.getEmail());
            return LoginResponse.builder()
                    .accessToken(accessToken)
                    .tokenType("Bearer")
                    .requiresTwoFactor(false)
                    .requiresPasswordChange(true)
                    .user(buildUserProfile(user))
                    .build();
        }

        return completeLogin(user, ipAddress, userAgent);
    }

    /**
     * Resends 2FA OTP to user's email.
     */
    @Override
    @Transactional
    public void resendOtp(ResendOtpRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", request.getEmail()));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidRequestException(ErrorMessages.ACCOUNT_INACTIVE);
        }

        TwoFactorConfig config = twoFactorConfigRepository.findByUserId(user.getId())
                .orElseThrow(() -> new InvalidRequestException("2FA is not configured for this user"));

        validatePendingLoginChallenge(config, request.getChallengeToken());
        generateAndSendOtp(user, config);
    }

    @Override
    @Transactional
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.getRefreshToken())
                .orElseThrow(() -> new SessionRevokedException(ErrorMessages.REFRESH_TOKEN_INVALID));

        if (refreshToken.getIsRevoked() || refreshToken.isExpired()) {
            if (refreshToken.isExpired() && !refreshToken.getIsRevoked()) {
                refreshToken.revoke();
                refreshTokenRepository.save(refreshToken);
            }
            throw new SessionRevokedException(ErrorMessages.REFRESH_TOKEN_INVALID);
        }

        // Check if session is revoked
        String fingerprint = EncryptionUtil.sha256Hash(request.getRefreshToken());
        UserSession session = userSessionRepository.findByTokenFingerprint(fingerprint)
                .orElseThrow(() -> new SessionRevokedException(ErrorMessages.REFRESH_TOKEN_REVOKED));
        if (session.getIsRevoked()) {
            throw new SessionRevokedException(ErrorMessages.REFRESH_TOKEN_REVOKED);
        }
        session.setLastActiveAt(LocalDateTime.now());
        userSessionRepository.save(session);

        User user = refreshToken.getUser();
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new InvalidRequestException(ErrorMessages.ACCOUNT_INACTIVE);
        }
        String newAccessToken = jwtTokenProvider.generateAccessTokenFromEmail(user.getEmail(), fingerprint);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(request.getRefreshToken()) // Return same refresh token
                .tokenType("Bearer")
                .requiresTwoFactor(false)
                .requiresPasswordChange(user.getForcePasswordChange())
                .user(buildUserProfile(user))
                .build();
    }

    /**
     * Logs out user by deleting their refresh token and revoking the session.
     */
    @Override
    @Transactional
    public void logout(String refreshToken, String email) {
        refreshTokenRepository.findByToken(refreshToken).ifPresent(token -> {
            if (!token.getUser().getEmail().equals(email)) {
                throw new InvalidRequestException(ErrorMessages.REFRESH_TOKEN_INVALID);
            }
            // Revoke the corresponding session
            String fingerprint = EncryptionUtil.sha256Hash(refreshToken);
            userSessionRepository.findByTokenFingerprint(fingerprint).ifPresent(session -> {
                session.setIsRevoked(true);
                userSessionRepository.save(session);
            });
            token.revoke();
            refreshTokenRepository.save(token);
        });
    }

    /**
     * Changes user password. Handles both forced and voluntary changes.
     * For forced changes, returns a full LoginResponse with tokens so the user
     * is fully authenticated after changing their password.
     */
    @Override
    @Transactional
    public LoginResponse changePassword(String email, ChangePasswordRequest request, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));

        // Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new InvalidRequestException("Current password is incorrect");
        }

        // Validate new password matches confirmation
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new InvalidRequestException("New password and confirmation do not match");
        }

        // Validate new password is different from current
        if (passwordEncoder.matches(request.getNewPassword(), user.getPasswordHash())) {
            throw new InvalidRequestException("New password must be different from current password");
        }

        boolean wasForcedChange = user.getForcePasswordChange();

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        user.setForcePasswordChange(false);
        user.setPasswordChangedAt(LocalDateTime.now());
        userRepository.save(user);

        auditService.log(user, "PASSWORD_CHANGED", ipAddress, "Password changed successfully");

        // If this was a forced password change, return full login response with tokens
        if (wasForcedChange) {
            return completeLogin(user, ipAddress, userAgent);
        }

        return null;
    }

    // --- Private Helpers ---

    /**
     * Completes the login flow by generating tokens and creating a session.
     */
    private LoginResponse completeLogin(User user, String ipAddress, String userAgent) {
        // Revoke all existing active sessions for this user first
        userSessionRepository.revokeByUserId(user.getId());

        String refreshTokenStr = jwtTokenProvider.generateRefreshToken(user.getEmail());
        String tokenFingerprint = EncryptionUtil.sha256Hash(refreshTokenStr);
        String accessToken = jwtTokenProvider.generateAccessTokenFromEmail(user.getEmail(), tokenFingerprint);

        // Save refresh token
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(refreshTokenStr)
                .expiresAt(LocalDateTime.now().plusSeconds(jwtTokenProvider.getRefreshTokenExpiryMs() / 1000))
                .build();
        refreshTokenRepository.save(refreshToken);

        // Create session record
        UserSession session = UserSession.builder()
                .user(user)
                .tokenFingerprint(tokenFingerprint)
                .deviceInfo(userAgent)
                .ipAddress(ipAddress)
                .lastActiveAt(LocalDateTime.now())
                .expiresAt(refreshToken.getExpiresAt())
                .build();
        userSessionRepository.save(session);

        auditService.log(user, "LOGIN_SUCCESS", ipAddress, "User logged in successfully");

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshTokenStr)
                .tokenType("Bearer")
                .requiresTwoFactor(false)
                .requiresPasswordChange(false)
                .user(buildUserProfile(user))
                .build();
    }

    /**
     * Handles failed login by incrementing counter and locking account if threshold
     * reached.
     */
    private void handleFailedLogin(User user, String ipAddress) {
        int attempts = user.getFailedLoginAttempts() + 1;
        user.setFailedLoginAttempts(attempts);

        if (attempts >= maxFailedAttempts) {
            user.setStatus(UserStatus.LOCKED);
            user.setLockedUntil(LocalDateTime.now().plusMinutes(lockoutDurationMinutes));
            log.warn("Account locked for user: {} after {} failed attempts", user.getEmail(), attempts);
            auditService.log(user, "ACCOUNT_LOCKED", ipAddress,
                    String.format("Account locked after %d failed login attempts", attempts));
        }

        userRepository.save(user);
        auditService.log(user, "LOGIN_FAILED", ipAddress,
                String.format("Failed login attempt #%d", attempts));
    }

    /**
     * Generates a random OTP, hashes it, stores it, and sends it via email.
     */
    private void generateAndSendOtp(User user, TwoFactorConfig config) {
        String otp = generateOtp();
        config.setOtpCode(passwordEncoder.encode(otp));
        config.setOtpExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        twoFactorConfigRepository.save(config);

        // Send OTP via email (async)
        emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp);
        log.info("2FA OTP sent to: {}", user.getEmail());
    }

    /**
     * Generates a random numeric OTP of the configured length.
     */
    private String generateOtp() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Creates a short-lived challenge proving the password step was completed.
     */
    private String createPendingLoginChallenge(TwoFactorConfig config) {
        byte[] randomBytes = new byte[32];
        new SecureRandom().nextBytes(randomBytes);
        String challengeToken = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        config.setPendingLoginTokenHash(EncryptionUtil.sha256Hash(challengeToken));
        config.setPendingLoginExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes));
        twoFactorConfigRepository.save(config);
        return challengeToken;
    }

    /**
     * Validates the pending-login challenge before allowing OTP operations.
     */
    private void validatePendingLoginChallenge(TwoFactorConfig config, String challengeToken) {
        if (!config.isPendingLoginValid()) {
            throw new InvalidRequestException(ErrorMessages.OTP_EXPIRED);
        }
        String providedHash = EncryptionUtil.sha256Hash(challengeToken);
        if (!providedHash.equals(config.getPendingLoginTokenHash())) {
            throw new InvalidRequestException(ErrorMessages.OTP_INVALID);
        }
    }

    /**
     * Builds the user profile portion of the login response.
     */
    private LoginResponse.UserProfile buildUserProfile(User user) {
        List<String> permissions = userPermissionRepository.findPermissionStringsByUserId(user.getId());

        return LoginResponse.UserProfile.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .profilePhotoUrl(user.getProfilePhotoUrl())
                .roleName(user.getRole().getName())
                .roleDisplayName(user.getRole().getDisplayName())
                .stateId(user.getState() != null ? user.getState().getId() : null)
                .stateName(user.getState() != null ? user.getState().getName() : null)
                .districtId(user.getDistrict() != null ? user.getDistrict().getId() : null)
                .districtName(user.getDistrict() != null ? user.getDistrict().getName() : null)
                .blockId(user.getBlock() != null ? user.getBlock().getId() : null)
                .blockName(user.getBlock() != null ? user.getBlock().getName() : null)
                .storeId(user.getStore() != null ? user.getStore().getId() : null)
                .storeName(user.getStore() != null ? user.getStore().getName() : null)
                .permissions(permissions)
                .build();
    }
}
