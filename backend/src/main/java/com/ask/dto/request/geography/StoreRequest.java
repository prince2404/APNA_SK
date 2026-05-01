package com.ask.dto.request.geography;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a store.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StoreRequest {

    @NotBlank(message = "Store name is required")
    @Size(max = 150, message = "Store name cannot exceed 150 characters")
    private String name;

    @NotBlank(message = "Store code is required")
    @Size(max = 20, message = "Store code cannot exceed 20 characters")
    private String code;

    private String address;

    @Size(max = 15, message = "Phone cannot exceed 15 characters")
    private String phone;

    @Size(max = 100, message = "Operating hours cannot exceed 100 characters")
    private String operatingHours;

    @NotNull(message = "Block ID is required")
    private Long blockId;
}
