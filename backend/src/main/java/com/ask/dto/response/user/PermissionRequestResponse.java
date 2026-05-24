package com.ask.dto.response.user;

import com.ask.enums.RequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Response DTO representing a permission request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequestResponse {
    private Long id;
    private Long userId;
    private String userEmail;
    private String userFullName;
    private Long permissionId;
    private String permissionModule;
    private String permissionAction;
    private String reason;
    private RequestStatus status;
    private Long reviewedById;
    private String reviewedByEmail;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
}
