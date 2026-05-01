package com.ask.dto.response.geography;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for store data.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StoreResponse {
    private Long id;
    private String name;
    private String code;
    private String address;
    private String phone;
    private String operatingHours;
    private Long blockId;
    private String blockName;
    private Long districtId;
    private String districtName;
    private Long stateId;
    private String stateName;
    private String status;
}
