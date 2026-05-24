package com.ask.dto.request.patient;

import com.ask.enums.Gender;
import com.ask.enums.MessagingPreference;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PatientRequest {

    @NotBlank(message = "Patient full name is required")
    private String fullName;

    private Integer age;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10,12}$", message = "Invalid phone number")
    private String phone;

    private String email;
    private String address;

    @NotNull(message = "State ID is required")
    private Long stateId;

    @NotNull(message = "District ID is required")
    private Long districtId;

    @NotNull(message = "Block ID is required")
    private Long blockId;

    private Long storeId;
    private Long hospitalId;
    
    @Builder.Default
    private MessagingPreference messagingPref = MessagingPreference.ALL;
}
