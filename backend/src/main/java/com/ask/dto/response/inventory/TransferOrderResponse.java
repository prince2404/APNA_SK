package com.ask.dto.response.inventory;

import com.ask.enums.TransferStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO representing stock transfer orders.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransferOrderResponse {
    private Long id;
    private String transferNumber;
    private Long storeId;
    private String storeName;
    private String createdByEmail;
    private TransferStatus status;
    private String notes;
    private String confirmedByEmail;
    private LocalDateTime confirmedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<TransferOrderItemResponse> items;
}
