package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.request.profile.BankDetailsRequest;
import com.ask.dto.request.profile.ProfileUpdateRequest;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.user.UserResponse;
import com.ask.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Controller for self-service user profile management.
 * All endpoints operate on the currently authenticated user's own data.
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.PROFILE)
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /** Get the currently authenticated user's own profile. */
    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                profileService.getMyProfile(userDetails.getUsername()),
                ApiPaths.PROFILE));
    }

    /** Update profile fields (name, phone, DOB, gender, address, Aadhaar last 4, PAN). */
    @PutMapping
    public ResponseEntity<ApiResponse<UserResponse>> updateMyProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                profileService.updateMyProfile(request, userDetails.getUsername()),
                "Profile updated successfully", ApiPaths.PROFILE));
    }

    /**
     * Upload or replace the profile photo.
     * Accepts multipart/form-data with a file parameter named {@code file}.
     */
    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> uploadProfilePhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(
                profileService.uploadProfilePhoto(file, userDetails.getUsername()),
                "Profile photo uploaded successfully", ApiPaths.PROFILE + "/photo"));
    }

    /**
     * Upload or replace the KYC document (Aadhaar, driving licence, passport PDF/image).
     * Resets verification status to PENDING for admin review.
     */
    @PostMapping(value = "/kyc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> uploadKycDocument(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.success(
                profileService.uploadKycDocument(file, userDetails.getUsername()),
                "KYC document uploaded successfully. Pending admin review.", ApiPaths.PROFILE + "/kyc"));
    }

    /**
     * Update bank details.
     * The account number is AES-256 encrypted before storage.
     */
    @PutMapping("/bank-details")
    public ResponseEntity<ApiResponse<UserResponse>> updateBankDetails(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BankDetailsRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                profileService.updateBankDetails(request, userDetails.getUsername()),
                "Bank details updated successfully", ApiPaths.PROFILE + "/bank-details"));
    }
}
