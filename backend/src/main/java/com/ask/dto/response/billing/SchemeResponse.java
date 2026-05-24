package com.ask.dto.response.billing;

import com.ask.enums.DiscountType;
import com.ask.enums.EntityStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class SchemeResponse {
    private Long id;
    private String name;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private Long categoryId;
    private String categoryName;
    private Long stateId;
    private String stateName;
    private LocalDate startDate;
    private LocalDate endDate;
    private EntityStatus status;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;
}
