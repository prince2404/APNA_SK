package com.ask.dto.response.geography;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for block data.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BlockResponse {
    private Long id;
    private String name;
    private Long districtId;
    private String districtName;
    private Long stateId;
    private String stateName;
    private String status;
    private int storeCount;
}
