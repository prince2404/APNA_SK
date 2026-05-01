package com.ask.dto.request.permission;

import com.ask.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for admins to approve or reject a permission request.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PermissionRequestReviewRequest {

    @NotNull(message = "Status is required (APPROVED or REJECTED)")
    private RequestStatus status;
}
