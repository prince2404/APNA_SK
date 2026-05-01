package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.request.verification.VerificationReviewRequest;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.user.UserResponse;
import com.ask.service.VerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for the KYC verification workflow.
 * Only Super Admin and System Admin can access these endpoints.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class VerificationController {

    private final VerificationService verificationService;

    /** List all users with PENDING verification status. */
    @GetMapping(ApiPaths.USERS + "/pending-verification")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getPendingVerifications(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                verificationService.getPendingVerifications(userDetails.getUsername(), page, size),
                ApiPaths.USERS + "/pending-verification"));
    }

    /** Approve or reject a user's KYC verification. */
    @PatchMapping(ApiPaths.USERS + "/{id}/verification")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> reviewVerification(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody VerificationReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                verificationService.reviewVerification(id, request, userDetails.getUsername()),
                "Verification status updated successfully",
                ApiPaths.USERS + "/" + id + "/verification"));
    }
}
