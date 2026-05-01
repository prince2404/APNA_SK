package com.ask.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Replaces a user's directly assigned checkbox permissions.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PermissionAssignmentRequest {

    @NotNull(message = "Permission IDs are required")
    private List<Long> permissionIds;
}
