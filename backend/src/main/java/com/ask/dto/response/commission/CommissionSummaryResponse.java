package com.ask.dto.response.commission;

import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CommissionSummaryResponse {
    private Long userId;
    private String fullName;
    private String roleName;
    private String month;
    private BigDecimal totalAmount;
}
