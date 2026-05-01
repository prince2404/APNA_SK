package com.ask.dto.request.profile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for updating bank details.
 * The account number will be AES-256 encrypted before storage.
 */
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class BankDetailsRequest {

    @NotBlank(message = "Bank account number is required")
    private String accountNumber;

    @NotBlank(message = "IFSC code is required")
    @Pattern(regexp = "^[A-Z]{4}0[A-Z0-9]{6}$", message = "Invalid IFSC code format")
    @Size(max = 20, message = "IFSC code cannot exceed 20 characters")
    private String ifscCode;

    @NotBlank(message = "Bank name is required")
    @Size(max = 100, message = "Bank name cannot exceed 100 characters")
    private String bankName;
}
