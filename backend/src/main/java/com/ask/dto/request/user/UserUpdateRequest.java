package com.ask.dto.request.user;

import com.ask.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Request DTO for updating user profile, role, and geography.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserUpdateRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name cannot exceed 150 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Size(max = 15, message = "Phone cannot exceed 15 characters")
    private String phone;

    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;
    private Long roleId;
    private Long stateId;
    private Long districtId;
    private Long blockId;
    private Long storeId;

    private String aadhaarLastFour;

    @Size(max = 10, message = "PAN cannot exceed 10 characters")
    private String panNumber;
}
