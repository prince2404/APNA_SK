package com.ask.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for an active user session.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserSessionResponse {
    private Long id;
    private String deviceInfo;
    private String ipAddress;
    private LocalDateTime lastActiveAt;
    private LocalDateTime expiresAt;
    private Boolean isRevoked;
    private LocalDateTime createdAt;
}
