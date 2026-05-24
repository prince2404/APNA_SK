package com.ask.dto.request.commission;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CommissionConfigRequest {

    @NotNull(message = "Role ID is required")
    private Long roleId;

    @NotNull(message = "Percentage is required")
    @DecimalMin(value = "0.00", message = "Percentage cannot be negative")
    @DecimalMax(value = "100.00", message = "Percentage cannot exceed 100")
    private BigDecimal percentage;
}
