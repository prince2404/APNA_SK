package com.ask.dto.request.product;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Request DTO for creating or updating products.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 200, message = "Name cannot exceed 200 characters")
    private String name;

    @Size(max = 100, message = "Brand cannot exceed 100 characters")
    private String brand;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @Size(max = 20, message = "HSN Code cannot exceed 20 characters")
    private String hsnCode;

    @NotNull(message = "MRP is required")
    @DecimalMin(value = "0.01", message = "MRP must be greater than 0")
    private BigDecimal mrp;

    @NotNull(message = "ASK Price is required")
    @DecimalMin(value = "0.01", message = "ASK Price must be greater than 0")
    private BigDecimal askPrice;

    @NotNull(message = "GST percentage is required")
    @DecimalMin(value = "0.0", message = "GST cannot be negative")
    private BigDecimal gstPercentage;

    @NotNull(message = "Min stock threshold is required")
    @Min(value = 0, message = "Threshold cannot be negative")
    private Integer minStockThreshold;
}
