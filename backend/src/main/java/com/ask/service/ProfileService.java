package com.ask.service;

import com.ask.dto.request.profile.BankDetailsRequest;
import com.ask.dto.request.profile.ProfileUpdateRequest;
import com.ask.dto.response.user.UserResponse;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for user profile management (self-service profile updates, photo upload, KYC, bank details).
 */
public interface ProfileService {

    /** Returns the currently authenticated user's profile. */
    UserResponse getMyProfile(String currentUserEmail);

    /** Updates personal profile fields for the currently authenticated user. */
    UserResponse updateMyProfile(ProfileUpdateRequest request, String currentUserEmail);

    /**
     * Stores the uploaded profile photo URL on the user record.
     * Actual file storage is handled by the caller (e.g., local disk or cloud storage).
     */
    UserResponse uploadProfilePhoto(MultipartFile file, String currentUserEmail);

    /**
     * Stores the uploaded KYC document URL on the user record and resets
     * verification status to PENDING so admins can review the new document.
     */
    UserResponse uploadKycDocument(MultipartFile file, String currentUserEmail);

    /**
     * Encrypts and saves bank account details for the currently authenticated user.
     * The account number is encrypted with AES-256 before being persisted.
     */
    UserResponse updateBankDetails(BankDetailsRequest request, String currentUserEmail);
}
