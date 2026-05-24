package com.ask.dto.response.inventory;

import com.ask.enums.AdjustmentType;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO representing store stock adjustments.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StockAdjustmentResponse {
    private Long id;
    private Long storeId;
    private String storeName;
    private Long productId;
    private String productName;
    private String batchNumber;
    private AdjustmentType adjustmentType;
    private Integer quantityChange;
    private String reason;
    private String adjustedByEmail;
    private LocalDateTime createdAt;
}
