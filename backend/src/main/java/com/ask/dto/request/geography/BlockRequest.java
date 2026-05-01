package com.ask.dto.request.geography;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a block.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BlockRequest {

    @NotBlank(message = "Block name is required")
    @Size(max = 100, message = "Block name cannot exceed 100 characters")
    private String name;

    @NotNull(message = "District ID is required")
    private Long districtId;
}
