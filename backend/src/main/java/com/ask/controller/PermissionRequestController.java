package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.request.user.PermissionRequestCreateRequest;
import com.ask.dto.request.user.RequestReviewRequest;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.user.PermissionRequestResponse;
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

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.PERMISSION_REQUESTS)
public class PermissionRequestController {

    private final PermissionRequestService permissionRequestService;

    @PostMapping
    public ResponseEntity<ApiResponse<PermissionRequestResponse>> createRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PermissionRequestCreateRequest request) {
        PermissionRequestResponse response = permissionRequestService.createRequest(
                request.getPermissionId(), request.getReason(), userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Permission request submitted successfully", ApiPaths.PERMISSION_REQUESTS));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PermissionRequestResponse>>> getRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<PermissionRequestResponse> response = permissionRequestService.getRequests(
                userDetails.getUsername(), status, page, size);
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.PERMISSION_REQUESTS));
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<PermissionRequestResponse>> reviewRequest(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody RequestReviewRequest request) {
        PermissionRequestResponse response = permissionRequestService.reviewRequest(
                id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Permission request reviewed successfully",
                ApiPaths.PERMISSION_REQUESTS + "/" + id + "/review"));
    }
}
