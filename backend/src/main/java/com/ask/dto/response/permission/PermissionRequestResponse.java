package com.ask.dto.response.permission;

import com.ask.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO for a permission request entry.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PermissionRequestResponse {
    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private Long permissionId;
    private String permissionCode;
    private String permissionDescription;
    private String reason;
    private RequestStatus status;
    private Long reviewedById;
    private String reviewedByFullName;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
