package com.ask.dto.response.commission;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CommissionConfigResponse {
    private Long id;
    private Long roleId;
    private String roleName;
    private BigDecimal percentage;
    private String updatedByName;
    private LocalDateTime updatedAt;
}
