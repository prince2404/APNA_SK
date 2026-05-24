package com.ask.dto.request.user;

import com.ask.enums.RequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for reviewing a permission request (approving/rejecting).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestReviewRequest {

    @NotNull(message = "Review status is required")
    private RequestStatus status;

    private String reason;
}
