package com.ask.dto.response.user;

import com.ask.enums.Gender;
import com.ask.enums.UserStatus;
import com.ask.enums.VerificationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for user management screens.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserResponse {
    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String profilePhotoUrl;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private Long roleId;
    private String roleName;
    private String roleDisplayName;
    private Integer hierarchyLevel;
    private Long stateId;
    private String stateName;
    private Long districtId;
    private String districtName;
    private Long blockId;
    private String blockName;
    private Long storeId;
    private String storeName;
    private String bankName;
    private String bankIfsc;
    private String bankAccount;
    private String aadhaarLastFour;
    private String panNumber;
    private VerificationStatus verificationStatus;
    private UserStatus status;
    private Boolean forcePasswordChange;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<String> permissions;
}
