package com.ask.dto.request.profile;

import com.ask.enums.Gender;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for users to update their own profile.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ProfileUpdateRequest {

    @Size(max = 150, message = "Full name cannot exceed 150 characters")
    private String fullName;

    @Size(max = 15, message = "Phone cannot exceed 15 characters")
    private String phone;

    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;

    /** Aadhaar last 4 digits only — 12-digit full number will be rejected */
    @Size(max = 4, message = "Only the last 4 digits of Aadhaar are accepted")
    private String aadhaarLastFour;

    @Size(max = 10, message = "PAN cannot exceed 10 characters")
    private String panNumber;
}
