package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.request.permission.PermissionRequestCreateRequest;
import com.ask.dto.request.permission.PermissionRequestReviewRequest;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.permission.PermissionRequestResponse;
import com.ask.enums.RequestStatus;
import com.ask.service.PermissionRequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for the permission request workflow.
 */
@Slf4j
@RestController
@RequestMapping(ApiPaths.PERMISSION_REQUESTS)
@RequiredArgsConstructor
public class PermissionRequestController {

    private final PermissionRequestService permissionRequestService;

    /**
     * Submit a new permission request.
     * Any authenticated user can request additional permissions.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PermissionRequestResponse>> createRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PermissionRequestCreateRequest request) {
        PermissionRequestResponse response =
                permissionRequestService.createRequest(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Permission request submitted successfully",
                        ApiPaths.PERMISSION_REQUESTS));
    }

    /**
     * Get the currently authenticated user's own permission requests.
     */
    @GetMapping("/my")
    public ResponseEntity<ApiResponse<PageResponse<PermissionRequestResponse>>> getMyRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                permissionRequestService.getMyRequests(userDetails.getUsername(), page, size),
                ApiPaths.PERMISSION_REQUESTS + "/my"));
    }

    /**
     * List all permission requests (Super/System Admin only).
     * Optionally filter by status.
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<PermissionRequestResponse>>> getAllRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) RequestStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                permissionRequestService.getAllRequests(status, userDetails.getUsername(), page, size),
                ApiPaths.PERMISSION_REQUESTS));
    }

    /**
     * Approve or reject a permission request.
     * Super/System Admin only.
     */
    @PatchMapping("/{id}/review")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<PermissionRequestResponse>> reviewRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequestReviewRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                permissionRequestService.reviewRequest(id, request, userDetails.getUsername()),
                "Permission request reviewed successfully",
                ApiPaths.PERMISSION_REQUESTS + "/" + id + "/review"));
    }
}
