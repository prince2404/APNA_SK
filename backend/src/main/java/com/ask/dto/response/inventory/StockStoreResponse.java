package com.ask.dto.response.inventory;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO representing store stock details.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StockStoreResponse {
    private Long id;
    private Long storeId;
    private String storeName;
    private Long productId;
    private String productName;
    private String productBrand;
    private String batchNumber;
    private LocalDate expiryDate;
    private Integer quantity;
    private LocalDateTime updatedAt;
}
