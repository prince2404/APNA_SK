package com.ask.dto.response.inventory;

import com.ask.enums.StockCentralStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Response DTO representing central stock warehouse details.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StockCentralResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String batchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private Integer quantity;
    private String receivedByEmail;
    private LocalDateTime receivedAt;
    private StockCentralStatus status;
}
