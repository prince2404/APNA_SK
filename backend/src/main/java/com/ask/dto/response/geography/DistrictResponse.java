package com.ask.dto.response.geography;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for district data.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DistrictResponse {
    private Long id;
    private String name;
    private Long stateId;
    private String stateName;
    private String status;
    private int blockCount;
}
