package com.ask.dto.request.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for resending a 2FA OTP during a pending login challenge.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ResendOtpRequest {

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "2FA challenge token is required")
    private String challengeToken;
}
