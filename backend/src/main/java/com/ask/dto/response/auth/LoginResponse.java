package com.ask.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Response DTO returned after successful login.
 * Contains tokens, user profile, role info, and permissions.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class LoginResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private UserProfile user;
    private boolean requiresTwoFactor;
    private boolean requiresPasswordChange;

    /**
     * Embedded user profile in the login response.
     */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class UserProfile {
        private Long id;
        private String fullName;
        private String email;
        private String phone;
        private String profilePhotoUrl;
        private String roleName;
        private String roleDisplayName;
        private Long stateId;
        private String stateName;
        private Long districtId;
        private String districtName;
        private Long blockId;
        private String blockName;
        private Long storeId;
        private String storeName;
        private List<String> permissions;
    }
}
