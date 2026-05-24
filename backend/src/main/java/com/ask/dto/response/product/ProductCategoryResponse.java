package com.ask.dto.response.product;

import com.ask.enums.EntityStatus;
import lombok.*;

/**
 * Response DTO representing product category details.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductCategoryResponse {
    private Long id;
    private String name;
    private EntityStatus status;
}
