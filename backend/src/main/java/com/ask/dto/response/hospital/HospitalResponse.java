package com.ask.dto.response.hospital;

import com.ask.enums.EntityStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class HospitalResponse {
    private Long id;
    private String name;
    private String address;
    private String phone;
    private String contactPerson;
    private Long stateId;
    private String stateName;
    private Long districtId;
    private String districtName;
    private EntityStatus status;
    private LocalDateTime createdAt;
}
