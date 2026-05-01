package com.ask.dto.response.geography;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for state data.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StateResponse {
    private Long id;
    private String name;
    private String code;
    private String status;
    private int districtCount;
}
