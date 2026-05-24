package com.ask.dto.request.inventory;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

/**
 * Request DTO for creating a stock transfer order.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class TransferOrderRequest {

    @NotNull(message = "Store ID is required")
    private Long storeId;

    private String notes;

    @NotEmpty(message = "Transfer items list cannot be empty")
    @Valid
    private List<TransferItemRequest> items;

    /**
     * Nested request DTO for individual batch items inside a transfer.
     */
    @Data @Builder @NoArgsConstructor @AllArgsConstructor
    public static class TransferItemRequest {
        @NotNull(message = "Product ID is required")
        private Long productId;

        @NotBlank(message = "Batch number is required")
        private String batchNumber;

        @NotNull(message = "Quantity is required")
        @Min(value = 1, message = "Quantity must be at least 1")
        private Integer quantity;
    }
}
