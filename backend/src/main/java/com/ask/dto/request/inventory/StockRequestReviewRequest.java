package com.ask.dto.request.inventory;

import com.ask.enums.StockRequestStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

/**
 * Request DTO for approving or rejecting store stock requests.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StockRequestReviewRequest {

    @NotNull(message = "Status is required")
    private StockRequestStatus status;

    private String notes;
}
