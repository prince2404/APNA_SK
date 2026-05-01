package com.ask.service;

import com.ask.dto.request.auth.*;
import com.ask.dto.response.auth.LoginResponse;

/**
 * Authentication service handling login, token refresh, logout, 2FA, and password management.
 */
public interface AuthService {

    /**
     * Authenticates a user with email and password.
     * If 2FA is required, returns a partial response with requiresTwoFactor=true.
     *
     * @param request   login credentials
     * @param ipAddress client IP for audit logging and session tracking
     * @param userAgent browser/device info for session tracking
     * @return login response with tokens and user profile
     */
    LoginResponse login(LoginRequest request, String ipAddress, String userAgent);

    /**
     * Verifies a 2FA OTP and completes the login flow.
     *
     * @param request   OTP verification data
     * @param ipAddress client IP
     * @param userAgent browser/device info
     * @return complete login response with tokens
     */
    LoginResponse verifyOtp(VerifyOtpRequest request, String ipAddress, String userAgent);

    /**
     * Resends a 2FA OTP to the user's email.
     *
     * @param email the user's email
     */
    void resendOtp(String email);

    /**
     * Refreshes the access token using a valid refresh token.
     *
     * @param request containing the refresh token
     * @return new access token
     */
    LoginResponse refreshToken(RefreshTokenRequest request);

    /**
     * Logs out the user by invalidating their refresh token and session.
     *
     * @param refreshToken the refresh token to invalidate
     */
    void logout(String refreshToken);

    /**
     * Changes the user's password. Used for both forced change and voluntary change.
     *
     * @param email   the authenticated user's email
     * @param request password change data
     */
    void changePassword(String email, ChangePasswordRequest request);
}
