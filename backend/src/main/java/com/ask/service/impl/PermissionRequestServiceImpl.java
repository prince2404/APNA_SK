package com.ask.service.impl;

import com.ask.constants.RoleConstants;
import com.ask.dto.request.user.RequestReviewRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.user.PermissionRequestResponse;
import com.ask.entity.Permission;
import com.ask.entity.PermissionRequest;
import com.ask.entity.User;
import com.ask.entity.UserPermission;
import com.ask.enums.NotificationType;
import com.ask.enums.RequestStatus;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.DuplicateResourceException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.repository.PermissionRepository;
import com.ask.repository.PermissionRequestRepository;
import com.ask.repository.UserPermissionRepository;
import com.ask.repository.UserRepository;
import com.ask.service.AuditService;
import com.ask.service.NotificationService;
import com.ask.service.PermissionRequestService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionRequestServiceImpl implements PermissionRequestService {

    private final PermissionRequestRepository permissionRequestRepository;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final NotificationService notificationService;
    private final AuditService auditService;

    @Override
    @Transactional
    public PermissionRequestResponse createRequest(Long permissionId, String reason, String email) {
        User user = getUser(email);
        Permission permission = permissionRepository.findById(permissionId)
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", permissionId));

        if (userPermissionRepository.existsByUserIdAndPermissionId(user.getId(), permissionId)) {
            throw new BusinessRuleException("User already has this permission assigned.");
        }

        if (permissionRequestRepository.existsByUserIdAndPermissionIdAndStatus(user.getId(), permissionId, RequestStatus.PENDING)) {
            throw new DuplicateResourceException("A pending permission request for this permission already exists.");
        }

        PermissionRequest request = PermissionRequest.builder()
                .user(user)
                .permission(permission)
                .reason(reason)
                .status(RequestStatus.PENDING)
                .build();

        PermissionRequest saved = permissionRequestRepository.save(request);

        // Notify Super/System Admins
        List<User> admins = userRepository.findByRoleNameIn(List.of(RoleConstants.SUPER_ADMIN, RoleConstants.SYSTEM_ADMIN));
        for (User admin : admins) {
            try {
                notificationService.sendNotification(
                        admin,
                        NotificationType.PERMISSION_REQUEST,
                        "New Permission Request",
                        "User " + user.getFullName() + " has requested permission: " + permission.getModule() + ":" + permission.getAction(),
                        "PERMISSION_REQUEST",
                        saved.getId()
                );
            } catch (Exception e) {
                log.error("Failed to send permission request notification to admin: {}", admin.getEmail(), e);
            }
        }

        auditService.log(user, "CREATE_PERMISSION_REQUEST", "PERMISSION_REQUEST", saved.getId(), null, null, null,
                "Requested permission " + permission.getModule() + ":" + permission.getAction());

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PermissionRequestResponse> getRequests(String email, String status, int page, int size) {
        User currentUser = getUser(email);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        RequestStatus statusEnum = null;
        if (status != null && !status.isBlank()) {
            try {
                statusEnum = RequestStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException("Invalid status filter: " + status);
            }
        }

        Page<PermissionRequest> requestPage;
        if (hasPlatformScope(currentUser)) {
            if (statusEnum != null) {
                requestPage = permissionRequestRepository.findByStatus(statusEnum, pageable);
            } else {
                requestPage = permissionRequestRepository.findAll(pageable);
            }
        } else {
            if (statusEnum != null) {
                requestPage = permissionRequestRepository.findByUserIdAndStatus(currentUser.getId(), statusEnum, pageable);
            } else {
                requestPage = permissionRequestRepository.findByUserId(currentUser.getId(), pageable);
            }
        }

        List<PermissionRequestResponse> content = requestPage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return PageResponse.of(requestPage, content);
    }

    @Override
    @Transactional
    public PermissionRequestResponse reviewRequest(Long id, RequestReviewRequest requestReview, String email) {
        User reviewer = getUser(email);

        if (!hasPlatformScope(reviewer)) {
            throw new BusinessRuleException("Only Super Admin and System Admin can review permission requests.");
        }

        PermissionRequest request = permissionRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("PermissionRequest", "id", id));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new BusinessRuleException("This permission request has already been reviewed.");
        }

        request.setStatus(requestReview.getStatus());
        request.setReviewedBy(reviewer);
        request.setReviewedAt(LocalDateTime.now());

        if (requestReview.getStatus() == RequestStatus.APPROVED) {
            if (!userPermissionRepository.existsByUserIdAndPermissionId(request.getUser().getId(), request.getPermission().getId())) {
                UserPermission grant = UserPermission.builder()
                        .user(request.getUser())
                        .permission(request.getPermission())
                        .grantedBy(reviewer)
                        .grantedAt(LocalDateTime.now())
                        .build();
                userPermissionRepository.save(grant);
            }
        }

        PermissionRequest saved = permissionRequestRepository.save(request);

        // Notify Requestor
        try {
            notificationService.sendNotification(
                    request.getUser(),
                    NotificationType.PERMISSION_REVIEW,
                    "Permission Request Reviewed",
                    "Your request for permission " + request.getPermission().getModule() + ":" + request.getPermission().getAction() +
                            " has been " + saved.getStatus().name().toLowerCase() + ".",
                    "PERMISSION_REQUEST",
                    saved.getId()
            );
        } catch (Exception e) {
            log.error("Failed to send review notification to user: {}", request.getUser().getEmail(), e);
        }

        auditService.log(reviewer, "REVIEW_PERMISSION_REQUEST", "PERMISSION_REQUEST", saved.getId(), null, null, null,
                "Reviewed permission request for " + request.getUser().getEmail() + " as " + saved.getStatus());

        return toResponse(saved);
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private boolean hasPlatformScope(User user) {
        String roleName = user.getRole().getName();
        return RoleConstants.SUPER_ADMIN.equals(roleName) || RoleConstants.SYSTEM_ADMIN.equals(roleName);
    }

    private PermissionRequestResponse toResponse(PermissionRequest request) {
        return PermissionRequestResponse.builder()
                .id(request.getId())
                .userId(request.getUser().getId())
                .userEmail(request.getUser().getEmail())
                .userFullName(request.getUser().getFullName())
                .permissionId(request.getPermission().getId())
                .permissionModule(request.getPermission().getModule())
                .permissionAction(request.getPermission().getAction())
                .reason(request.getReason())
                .status(request.getStatus())
                .reviewedById(request.getReviewedBy() != null ? request.getReviewedBy().getId() : null)
                .reviewedByEmail(request.getReviewedBy() != null ? request.getReviewedBy().getEmail() : null)
                .reviewedAt(request.getReviewedAt())
                .createdAt(request.getCreatedAt())
                .build();
    }
}
