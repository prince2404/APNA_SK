package com.ask.service.impl;

import com.ask.dto.request.auth.VerifyOtpRequest;
import com.ask.entity.Role;
import com.ask.entity.TwoFactorConfig;
import com.ask.entity.User;
import com.ask.enums.UserStatus;
import com.ask.exception.InvalidRequestException;
import com.ask.repository.RefreshTokenRepository;
import com.ask.repository.TwoFactorConfigRepository;
import com.ask.repository.UserPermissionRepository;
import com.ask.repository.UserRepository;
import com.ask.repository.UserSessionRepository;
import com.ask.security.JwtTokenProvider;
import com.ask.service.AuditService;
import com.ask.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserSessionRepository userSessionRepository;
    @Mock
    private TwoFactorConfigRepository twoFactorConfigRepository;
    @Mock
    private UserPermissionRepository userPermissionRepository;
    @Mock
    private AuditService auditService;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void verifyOtpRejectsOtpWithoutPendingPasswordChallenge() {
        ReflectionTestUtils.setField(authService, "otpExpiryMinutes", 5);

        User user = User.builder()
                .id(1L)
                .email("admin@askhealth.in")
                .fullName("Super Admin")
                .status(UserStatus.ACTIVE)
                .role(Role.builder().name("SUPER_ADMIN").displayName("Super Admin").build())
                .build();
        TwoFactorConfig config = TwoFactorConfig.builder()
                .user(user)
                .otpCode("hashed-otp")
                .otpExpiresAt(LocalDateTime.now().plusMinutes(5))
                .build();

        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        when(twoFactorConfigRepository.findByUserId(user.getId())).thenReturn(Optional.of(config));

        VerifyOtpRequest request = VerifyOtpRequest.builder()
                .email(user.getEmail())
                .otp("123456")
                .challengeToken("not-from-password-login")
                .build();

        assertThrows(InvalidRequestException.class,
                () -> authService.verifyOtp(request, "127.0.0.1", "JUnit"));
        verify(jwtTokenProvider, never()).generateAccessTokenFromEmail(user.getEmail());
        verify(refreshTokenRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
