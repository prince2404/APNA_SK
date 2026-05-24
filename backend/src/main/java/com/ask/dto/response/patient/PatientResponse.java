package com.ask.dto.response.patient;

import com.ask.enums.EntityStatus;
import com.ask.enums.Gender;
import com.ask.enums.MessagingPreference;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PatientResponse {
    private Long id;
    private String fullName;
    private Integer age;
    private Gender gender;
    private String phone;
    private String email;
    private String address;
    private Long stateId;
    private String stateName;
    private Long districtId;
    private String districtName;
    private Long blockId;
    private String blockName;
    private Long storeId;
    private String storeName;
    private Long hospitalId;
    private String hospitalName;
    private MessagingPreference messagingPref;
    private EntityStatus status;
    private LocalDateTime createdAt;
}
