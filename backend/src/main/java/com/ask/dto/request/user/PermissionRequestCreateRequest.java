package com.ask.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating a new permission request.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PermissionRequestCreateRequest {

    @NotNull(message = "Permission ID is required")
    private Long permissionId;

    @NotBlank(message = "Reason is required")
    private String reason;
}
