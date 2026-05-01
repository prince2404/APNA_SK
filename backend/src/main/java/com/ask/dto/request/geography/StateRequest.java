package com.ask.dto.request.geography;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for creating or updating a state.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StateRequest {

    @NotBlank(message = "State name is required")
    @Size(max = 100, message = "State name cannot exceed 100 characters")
    private String name;

    @NotBlank(message = "State code is required")
    @Size(max = 10, message = "State code cannot exceed 10 characters")
    private String code;
}
