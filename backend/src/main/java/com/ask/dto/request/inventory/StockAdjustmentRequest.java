package com.ask.dto.request.inventory;

import com.ask.enums.AdjustmentType;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * Request DTO for logging store stock adjustments.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StockAdjustmentRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Batch number is required")
    private String batchNumber;

    @NotNull(message = "Adjustment type is required")
    private AdjustmentType adjustmentType;

    @NotNull(message = "Quantity change is required")
    private Integer quantityChange;

    private String reason;
}
