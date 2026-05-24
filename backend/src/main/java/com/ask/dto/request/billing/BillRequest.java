package com.ask.dto.request.billing;

import com.ask.enums.PaymentMode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class BillRequest {

    @NotNull(message = "Patient ID is required")
    private Long patientId;

    @NotNull(message = "Payment mode is required")
    private PaymentMode paymentMode;

    @NotEmpty(message = "Items list cannot be empty")
    @Valid
    private List<BillItemRequest> items;
}
