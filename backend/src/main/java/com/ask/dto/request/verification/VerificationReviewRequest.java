package com.ask.dto.request.verification;

import com.ask.enums.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for Super/System Admin to approve or reject a KYC verification.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class VerificationReviewRequest {

    @NotNull(message = "Verification status is required")
    private VerificationStatus status;

    private String remarks;
}
