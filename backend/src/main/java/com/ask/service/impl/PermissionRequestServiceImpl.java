package com.ask.service.impl;

import com.ask.constants.AppConstants;
import com.ask.constants.ErrorMessages;
import com.ask.constants.RoleConstants;
import com.ask.dto.request.permission.PermissionRequestCreateRequest;
import com.ask.dto.request.permission.PermissionRequestReviewRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.permission.PermissionRequestResponse;
import com.ask.entity.Permission;
import com.ask.entity.PermissionRequest;
import com.ask.entity.User;
import com.ask.entity.UserPermission;
import com.ask.enums.NotificationType;
import com.ask.enums.RequestStatus;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.InvalidRequestException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.mapper.PermissionRequestMapper;
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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Permission request workflow implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionRequestServiceImpl implements PermissionRequestService {

    private final PermissionRequestRepository permissionRequestRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final PermissionRequestMapper permissionRequestMapper;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public PermissionRequestResponse createRequest(PermissionRequestCreateRequest request,
                                                    String currentUserEmail) {
        User user = getUser(currentUserEmail);
        Permission permission = permissionRepository.findById(request.getPermissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", request.getPermissionId()));

        // Reject if user already has the permission
        if (userPermissionRepository.existsByUserIdAndPermissionId(user.getId(), permission.getId())) {
            throw new BusinessRuleException("You already have this permission");
        }

        // Reject if there is already a pending request for the same permission
        if (permissionRequestRepository.existsByUserIdAndPermissionIdAndStatus(
                user.getId(), permission.getId(), RequestStatus.PENDING)) {
            throw new BusinessRuleException("A pending request for this permission already exists");
        }

        PermissionRequest permReq = PermissionRequest.builder()
                .user(user)
                .permission(permission)
                .reason(request.getReason())
                .status(RequestStatus.PENDING)
                .build();

        PermissionRequest saved = permissionRequestRepository.save(permReq);
        auditService.log(user, "REQUEST_PERMISSION", "PERMISSION_REQUEST", saved.getId(), null, null, null,
                "Permission request submitted for: " + permission.getModule() + ":" + permission.getAction());

        log.info("Permission request {} created by user {} for permission {}:{}",
                saved.getId(), user.getEmail(), permission.getModule(), permission.getAction());
        return permissionRequestMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PermissionRequestResponse> getMyRequests(String currentUserEmail, int page, int size) {
        User user = getUser(currentUserEmail);
        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Page<PermissionRequest> pageResult = permissionRequestRepository.findByUserId(
                user.getId(), PageRequest.of(page, size, Sort.by("createdAt").descending()));
        List<PermissionRequestResponse> content = pageResult.getContent().stream()
                .map(permissionRequestMapper::toResponse).toList();
        return PageResponse.of(pageResult, content);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PermissionRequestResponse> getAllRequests(RequestStatus status, String currentUserEmail,
                                                                   int page, int size) {
        User currentUser = getUser(currentUserEmail);
        ensureAdminRole(currentUser);

        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Page<PermissionRequest> pageResult;
        if (status != null) {
            pageResult = permissionRequestRepository.findByStatus(
                    status, PageRequest.of(page, size, Sort.by("createdAt").descending()));
        } else {
            pageResult = permissionRequestRepository.findAll(
                    PageRequest.of(page, size, Sort.by("createdAt").descending()));
        }
        List<PermissionRequestResponse> content = pageResult.getContent().stream()
                .map(permissionRequestMapper::toResponse).toList();
        return PageResponse.of(pageResult, content);
    }

    @Override
    @Transactional
    public PermissionRequestResponse reviewRequest(Long requestId,
                                                    PermissionRequestReviewRequest request,
                                                    String currentUserEmail) {
        User currentUser = getUser(currentUserEmail);
        ensureAdminRole(currentUser);

        PermissionRequest permReq = permissionRequestRepository.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("PermissionRequest", "id", requestId));

        if (permReq.getStatus() != RequestStatus.PENDING) {
            throw new InvalidRequestException("This request has already been reviewed");
        }
        if (request.getStatus() == RequestStatus.PENDING) {
            throw new InvalidRequestException("Cannot set status back to PENDING");
        }

        permReq.setStatus(request.getStatus());
        permReq.setReviewedBy(currentUser);
        permReq.setReviewedAt(LocalDateTime.now());

        // Auto-grant the permission on approval
        if (request.getStatus() == RequestStatus.APPROVED) {
            if (!userPermissionRepository.existsByUserIdAndPermissionId(
                    permReq.getUser().getId(), permReq.getPermission().getId())) {
                userPermissionRepository.save(UserPermission.builder()
                        .user(permReq.getUser())
                        .permission(permReq.getPermission())
                        .grantedBy(currentUser)
                        .grantedAt(LocalDateTime.now())
                        .build());
            }
        }

        PermissionRequest saved = permissionRequestRepository.save(permReq);
        auditService.log(currentUser, "REVIEW_PERMISSION_REQUEST", "PERMISSION_REQUEST", saved.getId(),
                null, null, null,
                String.format("Permission request %s for user %s (%s:%s)",
                        request.getStatus(), permReq.getUser().getEmail(),
                        permReq.getPermission().getModule(), permReq.getPermission().getAction()));

        // Notify the requesting user
        boolean approved = request.getStatus() == RequestStatus.APPROVED;
        notificationService.createNotification(
                permReq.getUser(),
                approved ? NotificationType.PERMISSION_REQUEST_APPROVED
                         : NotificationType.PERMISSION_REQUEST_REJECTED,
                approved ? "Permission Request Approved" : "Permission Request Rejected",
                approved
                        ? String.format("Your request for %s:%s has been approved.",
                                permReq.getPermission().getModule(), permReq.getPermission().getAction())
                        : String.format("Your request for %s:%s has been rejected.",
                                permReq.getPermission().getModule(), permReq.getPermission().getAction()),
                "PERMISSION_REQUEST",
                saved.getId()
        );

        log.info("Permission request {} reviewed as {} by {}", saved.getId(),
                request.getStatus(), currentUser.getEmail());
        return permissionRequestMapper.toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private void ensureAdminRole(User user) {
        String role = user.getRole().getName();
        if (!RoleConstants.SUPER_ADMIN.equals(role) && !RoleConstants.SYSTEM_ADMIN.equals(role)) {
            throw new BusinessRuleException(ErrorMessages.ACCESS_DENIED);
        }
    }
}
