package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.constants.AppConstants;
import com.ask.dto.response.audit.AuditLogResponse;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.common.PageResponse;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.GeographicScopeException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.mapper.AuditLogMapper;
import com.ask.repository.AuditLogRepository;
import com.ask.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for user activity log queries.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class ActivityLogController {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;

    /**
     * Get activity logs for the currently authenticated user (own logs).
     */
    @GetMapping(ApiPaths.PROFILE + "/activity")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getMyActivity(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));

        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        var pageResult = auditLogRepository.findByUserId(currentUser.getId(),
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        List<AuditLogResponse> content = pageResult.getContent().stream()
                .map(auditLogMapper::toResponse).toList();

        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(pageResult, content),
                ApiPaths.PROFILE + "/activity"));
    }

    /**
     * Get activity logs for a specific user by ID.
     * Accessible to Super Admin and System Admin only.
     */
    @GetMapping(ApiPaths.USERS + "/{id}/activity")
    @PreAuthorize("hasRole('SUPER_ADMIN') or hasRole('SYSTEM_ADMIN') or hasAuthority('PERM_USERS_VIEW')")
    public ResponseEntity<ApiResponse<PageResponse<AuditLogResponse>>> getUserActivity(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var currentUser = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", userDetails.getUsername()));
        var targetUser = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        // Non-platform admins can only view users within their geographic scope
        if (!isPlatformAdmin(currentUser)) {
            ensureGeographicScope(currentUser, targetUser);
        }

        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        var pageResult = auditLogRepository.findByUserId(id,
                PageRequest.of(page, size, Sort.by("createdAt").descending()));
        List<AuditLogResponse> content = pageResult.getContent().stream()
                .map(auditLogMapper::toResponse).toList();

        return ResponseEntity.ok(ApiResponse.success(
                PageResponse.of(pageResult, content),
                ApiPaths.USERS + "/" + id + "/activity"));
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private boolean isPlatformAdmin(com.ask.entity.User user) {
        String role = user.getRole().getName();
        return com.ask.constants.RoleConstants.SUPER_ADMIN.equals(role)
                || com.ask.constants.RoleConstants.SYSTEM_ADMIN.equals(role);
    }

    private void ensureGeographicScope(com.ask.entity.User current, com.ask.entity.User target) {
        if (current.getState() != null && target.getState() != null
                && !current.getState().getId().equals(target.getState().getId())) {
            throw new GeographicScopeException();
        }
    }
}
