package com.ask.dto.request.inventory;

import com.ask.enums.Urgency;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for creating store stock requests.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StockRequestRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotNull(message = "Quantity requested is required")
    @Min(value = 1, message = "Quantity requested must be at least 1")
    private Integer quantityRequested;

    @NotNull(message = "Urgency level is required")
    private Urgency urgency;

    private String notes;
}
