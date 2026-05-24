package com.ask.dto.request.hospital;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class HospitalRequest {

    @NotBlank(message = "Hospital name is required")
    private String name;

    private String address;
    private String phone;
    private String contactPerson;

    @NotNull(message = "State ID is required")
    private Long stateId;

    @NotNull(message = "District ID is required")
    private Long districtId;
}
