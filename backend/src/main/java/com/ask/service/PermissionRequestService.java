package com.ask.service;

import com.ask.dto.request.user.RequestReviewRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.user.PermissionRequestResponse;

/**
 * Service interface for handling permission requests and their reviews.
 */
public interface PermissionRequestService {

    /**
     * Creates a new permission request.
     *
     * @param permissionId ID of the permission requested
     * @param reason reason for requesting
     * @param email requestor's email
     * @return PermissionRequestResponse
     */
    PermissionRequestResponse createRequest(Long permissionId, String reason, String email);

    /**
     * Retrieves permission requests. Normal users can only see their own requests.
     * Super/System Admin can see all requests.
     *
     * @param email current logged-in user email
     * @param status optional status filter (PENDING, APPROVED, REJECTED)
     * @param page page number
     * @param size page size
     * @return PageResponse of PermissionRequestResponse
     */
    PageResponse<PermissionRequestResponse> getRequests(String email, String status, int page, int size);

    /**
     * Reviews (approves/rejects) a permission request.
     * Only Super/System Admins can review requests.
     *
     * @param id permission request ID
     * @param request review request details
     * @param email reviewer's email
     * @return PermissionRequestResponse
     */
    PermissionRequestResponse reviewRequest(Long id, RequestReviewRequest request, String email);
}
