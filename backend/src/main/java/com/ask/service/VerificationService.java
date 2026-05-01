package com.ask.service;

import com.ask.dto.request.verification.VerificationReviewRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.user.UserResponse;

/**
 * Service for the KYC verification workflow.
 * Super/System Admins review pending verifications and approve or reject them.
 */
public interface VerificationService {

    /**
     * Returns a paginated list of users whose verificationStatus is PENDING.
     * Scoped to the calling admin's geographic scope.
     */
    PageResponse<UserResponse> getPendingVerifications(String currentUserEmail, int page, int size);

    /**
     * Approves or rejects a user's KYC verification.
     * Sends an in-app notification to the user on outcome.
     */
    UserResponse reviewVerification(Long userId, VerificationReviewRequest request, String currentUserEmail);
}
