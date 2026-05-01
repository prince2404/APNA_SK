package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.request.auth.*;
import com.ask.dto.response.auth.LoginResponse;
import com.ask.dto.response.common.ApiResponse;
import com.ask.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication controller handling login, 2FA, token refresh, logout, and password change.
 * All auth endpoints are public except change-password and logout.
 *
 * Endpoints:
 * POST /api/v1/auth/login         — Authenticate with email + password
 * POST /api/v1/auth/verify-otp    — Verify 2FA OTP
 * POST /api/v1/auth/resend-otp    — Resend 2FA OTP
 * POST /api/v1/auth/refresh       — Refresh access token
 * POST /api/v1/auth/logout        — Logout and invalidate refresh token
 * POST /api/v1/auth/change-password — Change password (forced or voluntary)
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.AUTH)
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/login
     * Authenticates a user with email and password.
     * Returns tokens + user profile on success, or 2FA pending status.
     * Accessible by: Everyone (no authentication required)
     *
     * @param request   login credentials
     * @param httpRequest for extracting IP and user agent
     * @return LoginResponse with tokens and user profile
     */
    @PostMapping(ApiPaths.AUTH_LOGIN)
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        String ipAddress = getClientIp(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        LoginResponse response = authService.login(request, ipAddress, userAgent);

        String message = response.isRequiresTwoFactor()
                ? "OTP sent to your email. Please verify to complete login"
                : response.isRequiresPasswordChange()
                    ? "Login successful. Please change your password to continue"
                    : "Login successful";

        return ResponseEntity.ok(ApiResponse.success(response, message, ApiPaths.AUTH + ApiPaths.AUTH_LOGIN));
    }

    /**
     * POST /api/v1/auth/verify-otp
     * Verifies 2FA OTP and completes login.
     * Accessible by: Users with pending 2FA verification
     */
    @PostMapping(ApiPaths.AUTH_VERIFY_OTP)
    public ResponseEntity<ApiResponse<LoginResponse>> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request,
            HttpServletRequest httpRequest) {

        LoginResponse response = authService.verifyOtp(request,
                getClientIp(httpRequest), httpRequest.getHeader("User-Agent"));
        return ResponseEntity.ok(ApiResponse.success(response, "OTP verified successfully",
                ApiPaths.AUTH + ApiPaths.AUTH_VERIFY_OTP));
    }

    /**
     * POST /api/v1/auth/resend-otp
     * Resends 2FA OTP to user's email.
     * Accessible by: Users with pending 2FA verification
     */
    @PostMapping(ApiPaths.AUTH_RESEND_OTP)
    public ResponseEntity<ApiResponse<Void>> resendOtp(@RequestParam String email) {
        authService.resendOtp(email);
        return ResponseEntity.ok(ApiResponse.success(null, "OTP resent to your email",
                ApiPaths.AUTH + ApiPaths.AUTH_RESEND_OTP));
    }

    /**
     * POST /api/v1/auth/refresh
     * Refreshes the access token using a valid refresh token.
     * Accessible by: Any authenticated user with a valid refresh token
     */
    @PostMapping(ApiPaths.AUTH_REFRESH)
    public ResponseEntity<ApiResponse<LoginResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        LoginResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed",
                ApiPaths.AUTH + ApiPaths.AUTH_REFRESH));
    }

    /**
     * POST /api/v1/auth/logout
     * Logs out user and invalidates their refresh token.
     * Accessible by: Any authenticated user
     */
    @PostMapping(ApiPaths.AUTH_LOGOUT)
    public ResponseEntity<ApiResponse<Void>> logout(
            @Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully",
                ApiPaths.AUTH + ApiPaths.AUTH_LOGOUT));
    }

    /**
     * POST /api/v1/auth/change-password
     * Changes the authenticated user's password.
     * Accessible by: Any authenticated user
     */
    @PostMapping(ApiPaths.AUTH_CHANGE_PASSWORD)
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully",
                ApiPaths.AUTH + ApiPaths.AUTH_CHANGE_PASSWORD));
    }

    /**
     * Extracts the client IP address, accounting for proxies.
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
