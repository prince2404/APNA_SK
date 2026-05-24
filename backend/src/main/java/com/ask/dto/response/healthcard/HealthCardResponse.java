package com.ask.dto.response.healthcard;

import com.ask.enums.EntityStatus;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class HealthCardResponse {
    private Long id;
    private String cardNumber;
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private Long storeId;
    private String storeName;
    private Long issuedBy;
    private String issuedByName;
    private LocalDateTime issuedAt;
    private EntityStatus status;
    private List<HealthCardMemberResponse> members;
}
