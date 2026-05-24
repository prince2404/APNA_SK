package com.ask.dto.response.billing;

import com.ask.enums.ReturnStatus;
import lombok.*;

import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BillItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productBrand;
    private String batchNumber;
    private Integer quantity;
    private BigDecimal mrp;
    private BigDecimal askPrice;
    private BigDecimal gstAmount;
    private BigDecimal discountAmount;
    private BigDecimal subtotal;
    private ReturnStatus returnStatus;
    private Integer returnQuantity;
    private String returnReason;
}
