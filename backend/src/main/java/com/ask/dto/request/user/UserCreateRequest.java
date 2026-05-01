package com.ask.dto.request.user;

import com.ask.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * Request DTO for creating users from the user management module.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class UserCreateRequest {

    @NotBlank(message = "Full name is required")
    @Size(max = 150, message = "Full name cannot exceed 150 characters")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    @Size(max = 150, message = "Email cannot exceed 150 characters")
    private String email;

    @Size(max = 15, message = "Phone cannot exceed 15 characters")
    private String phone;

    @NotBlank(message = "Temporary password is required")
    @Size(min = 8, max = 100, message = "Temporary password must be between 8 and 100 characters")
    private String temporaryPassword;

    private LocalDate dateOfBirth;
    private Gender gender;
    private String address;

    @NotNull(message = "Role ID is required")
    private Long roleId;

    private Long stateId;
    private Long districtId;
    private Long blockId;
    private Long storeId;

    private String aadhaarLastFour;

    @Size(max = 10, message = "PAN cannot exceed 10 characters")
    private String panNumber;

    private List<Long> permissionIds;
}
