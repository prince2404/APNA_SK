package com.ask.dto.response.inventory;

import com.ask.enums.StockRequestStatus;
import com.ask.enums.Urgency;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Response DTO representing store stock requests.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StockRequestResponse {
    private Long id;
    private Long storeId;
    private String storeName;
    private Long productId;
    private String productName;
    private Integer quantityRequested;
    private Urgency urgency;
    private StockRequestStatus status;
    private String requestedByEmail;
    private String reviewedByEmail;
    private LocalDateTime reviewedAt;
    private String notes;
    private LocalDateTime createdAt;
}
