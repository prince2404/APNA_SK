package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.request.user.PermissionAssignmentRequest;
import com.ask.dto.request.user.UserCreateRequest;
import com.ask.dto.request.user.UserUpdateRequest;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.user.PermissionResponse;
import com.ask.dto.response.user.UserResponse;
import com.ask.dto.request.user.KycReviewRequest;
import com.ask.enums.UserStatus;
import com.ask.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for admin user management and direct permission assignment.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping(ApiPaths.USERS)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_USERS_CREATE')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserCreateRequest request) {
        UserResponse response = userService.createUser(request, userDetails.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User created successfully", ApiPaths.USERS));
    }

    @GetMapping(ApiPaths.USERS)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_USERS_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsers(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long roleId,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getUsers(userDetails.getUsername(), search, roleId, status, page, size),
                ApiPaths.USERS));
    }

    @GetMapping(ApiPaths.USERS + "/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_USERS_VIEW')")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.getUserById(id, userDetails.getUsername()), ApiPaths.USERS + "/" + id));
    }

    @PutMapping(ApiPaths.USERS + "/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_USERS_EDIT')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.updateUser(id, request, userDetails.getUsername()),
                "User updated successfully", ApiPaths.USERS + "/" + id));
    }

    @PatchMapping(ApiPaths.USERS + "/{id}/deactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deactivateUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        userService.deactivateUser(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "User deactivated successfully",
                ApiPaths.USERS + "/" + id + "/deactivate"));
    }

    @PatchMapping(ApiPaths.USERS + "/{id}/reactivate")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> reactivateUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        userService.reactivateUser(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "User reactivated successfully",
                ApiPaths.USERS + "/" + id + "/reactivate"));
    }

    @PutMapping(ApiPaths.USERS + "/{id}/permissions")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_USERS_EDIT')")
    public ResponseEntity<ApiResponse<UserResponse>> assignPermissions(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody PermissionAssignmentRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                userService.assignPermissions(id, request, userDetails.getUsername()),
                "User permissions updated successfully", ApiPaths.USERS + "/" + id + "/permissions"));
    }

    @GetMapping(ApiPaths.PERMISSIONS)
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_USERS_VIEW') or hasAuthority('PERM_USERS_EDIT')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getPermissions() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllPermissions(), ApiPaths.PERMISSIONS));
    }

    @GetMapping(ApiPaths.USERS + "/verification-queue")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_USERS_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getVerificationQueue(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<UserResponse> response = userService.getVerificationQueue(userDetails.getUsername(), page, size);
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.USERS + "/verification-queue"));
    }

    @PostMapping(ApiPaths.USERS + "/{id}/verify")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> verifyUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody KycReviewRequest request) {
        UserResponse response = userService.verifyUser(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "User KYC status updated successfully", ApiPaths.USERS + "/" + id + "/verify"));
    }

    @GetMapping(ApiPaths.USERS + "/{id}/kyc/document")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasAuthority('PERM_USERS_VIEW')")
    public ResponseEntity<Resource> downloadUserKycDocument(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        Resource resource = userService.getUserKycDocument(id, userDetails.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
