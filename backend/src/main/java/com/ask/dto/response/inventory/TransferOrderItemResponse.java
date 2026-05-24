package com.ask.dto.response.inventory;

import lombok.*;

import java.time.LocalDate;

/**
 * Response DTO representing detailed items inside a transfer order.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransferOrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String batchNumber;
    private LocalDate expiryDate;
    private Integer quantity;
}
