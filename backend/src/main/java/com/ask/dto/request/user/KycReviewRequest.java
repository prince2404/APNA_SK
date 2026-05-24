package com.ask.dto.request.user;

import com.ask.enums.VerificationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for reviewing a user's KYC details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycReviewRequest {

    @NotNull(message = "Verification status is required")
    private VerificationStatus status;

    private String reason;
}
