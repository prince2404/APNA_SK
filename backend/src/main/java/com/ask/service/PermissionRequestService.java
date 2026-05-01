package com.ask.service;

import com.ask.dto.request.permission.PermissionRequestCreateRequest;
import com.ask.dto.request.permission.PermissionRequestReviewRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.permission.PermissionRequestResponse;
import com.ask.enums.RequestStatus;

/**
 * Service for the permission request workflow.
 */
public interface PermissionRequestService {

    /** Creates a new permission request for the currently authenticated user. */
    PermissionRequestResponse createRequest(PermissionRequestCreateRequest request, String currentUserEmail);

    /** Returns the currently authenticated user's own permission requests. */
    PageResponse<PermissionRequestResponse> getMyRequests(String currentUserEmail, int page, int size);

    /**
     * Returns all permission requests, optionally filtered by status.
     * Available to Super/System Admins only.
     */
    PageResponse<PermissionRequestResponse> getAllRequests(RequestStatus status, String currentUserEmail,
                                                           int page, int size);

    /**
     * Approves or rejects a permission request.
     * If approved, the permission is automatically granted to the user.
     */
    PermissionRequestResponse reviewRequest(Long requestId, PermissionRequestReviewRequest request,
                                             String currentUserEmail);
}
