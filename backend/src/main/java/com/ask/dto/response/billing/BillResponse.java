package com.ask.dto.response.billing;

import com.ask.enums.BillStatus;
import com.ask.enums.PaymentMode;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BillResponse {
    private Long id;
    private String billNumber;
    private Long storeId;
    private String storeName;
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private Long healthCardId;
    private String healthCardNumber;
    private BigDecimal totalMrp;
    private BigDecimal totalAskPrice;
    private BigDecimal totalGst;
    private BigDecimal totalDiscount;
    private BigDecimal netAmount;
    private BigDecimal totalSavings;
    private PaymentMode paymentMode;
    private BillStatus status;
    private String cancelReason;
    private Long cancelledBy;
    private String cancelledByName;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime billDate;
    private List<BillItemResponse> items;
}
