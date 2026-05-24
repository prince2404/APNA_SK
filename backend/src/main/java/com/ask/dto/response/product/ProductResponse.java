package com.ask.dto.response.product;

import com.ask.enums.EntityStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO representing product catalog details.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private String brand;
    private Long categoryId;
    private String categoryName;
    private String hsnCode;
    private BigDecimal mrp;
    private BigDecimal askPrice;
    private BigDecimal gstPercentage;
    private Integer minStockThreshold;
    private EntityStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
