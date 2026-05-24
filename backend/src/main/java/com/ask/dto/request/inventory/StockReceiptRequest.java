package com.ask.dto.request.inventory;

import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Request DTO for receiving stock at the central warehouse.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StockReceiptRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    @NotBlank(message = "Batch number is required")
    @Size(max = 50, message = "Batch number cannot exceed 50 characters")
    private String batchNumber;

    private LocalDate manufacturingDate;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;
}
