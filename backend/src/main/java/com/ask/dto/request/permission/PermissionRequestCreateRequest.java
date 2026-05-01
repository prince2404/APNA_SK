package com.ask.dto.request.permission;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for a user to request an additional permission.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PermissionRequestCreateRequest {

    @NotNull(message = "Permission ID is required")
    private Long permissionId;

    @Size(max = 1000, message = "Reason cannot exceed 1000 characters")
    private String reason;
}
