package com.ask.service.impl;

import com.ask.constants.AppConstants;
import com.ask.constants.ErrorMessages;
import com.ask.constants.RoleConstants;
import com.ask.dto.request.verification.VerificationReviewRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.user.UserResponse;
import com.ask.entity.User;
import com.ask.enums.NotificationType;
import com.ask.enums.VerificationStatus;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.GeographicScopeException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.mapper.UserMapper;
import com.ask.repository.UserPermissionRepository;
import com.ask.repository.UserRepository;
import com.ask.service.AuditService;
import com.ask.service.NotificationService;
import com.ask.service.VerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * KYC verification workflow implementation.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationServiceImpl implements VerificationService {

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final UserMapper userMapper;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getPendingVerifications(String currentUserEmail, int page, int size) {
        User currentUser = getCurrentUser(currentUserEmail);
        ensureCanReviewVerifications(currentUser);

        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Specification<User> spec = (root, query, cb) -> {
            var predicate = cb.equal(root.get("verificationStatus"), VerificationStatus.PENDING);
            if (!hasPlatformScope(currentUser)) {
                // Scope to the admin's geographic region
                if (currentUser.getState() != null) {
                    predicate = cb.and(predicate,
                            cb.equal(root.get("state").get("id"), currentUser.getState().getId()));
                }
            }
            return predicate;
        };

        Page<User> userPage = userRepository.findAll(spec,
                PageRequest.of(page, size, Sort.by("updatedAt").descending()));
        List<UserResponse> content = userPage.getContent().stream().map(this::toResponse).toList();
        return PageResponse.of(userPage, content);
    }

    @Override
    @Transactional
    public UserResponse reviewVerification(Long userId, VerificationReviewRequest request,
                                           String currentUserEmail) {
        User currentUser = getCurrentUser(currentUserEmail);
        ensureCanReviewVerifications(currentUser);

        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!hasPlatformScope(currentUser)) {
            ensureUserInScope(currentUser, targetUser);
        }

        if (request.getStatus() == VerificationStatus.PENDING) {
            throw new BusinessRuleException("Cannot set verification status back to PENDING");
        }

        VerificationStatus previousStatus = targetUser.getVerificationStatus();
        targetUser.setVerificationStatus(request.getStatus());
        User saved = userRepository.save(targetUser);

        auditService.log(currentUser, "REVIEW_VERIFICATION", "USER", saved.getId(), null, null, null,
                String.format("KYC verification %s for user %s by %s. Remarks: %s",
                        request.getStatus(), saved.getEmail(), currentUser.getEmail(),
                        request.getRemarks() != null ? request.getRemarks() : ""));

        // Send in-app notification to the user
        boolean approved = request.getStatus() == VerificationStatus.VERIFIED;
        notificationService.createNotification(
                saved,
                approved ? NotificationType.VERIFICATION_APPROVED : NotificationType.VERIFICATION_REJECTED,
                approved ? "KYC Verification Approved" : "KYC Verification Rejected",
                approved
                        ? "Your KYC documents have been verified successfully."
                        : "Your KYC verification was rejected. " +
                          (request.getRemarks() != null ? "Reason: " + request.getRemarks() : "Please re-upload documents."),
                "USER",
                saved.getId()
        );

        log.info("Verification status changed from {} to {} for user {} by {}",
                previousStatus, request.getStatus(), saved.getEmail(), currentUser.getEmail());
        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private User getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private UserResponse toResponse(User user) {
        UserResponse response = userMapper.toUserResponse(user);
        response.setPermissions(userPermissionRepository.findPermissionStringsByUserId(user.getId()));
        return response;
    }

    private void ensureCanReviewVerifications(User user) {
        String role = user.getRole().getName();
        if (!RoleConstants.SUPER_ADMIN.equals(role) && !RoleConstants.SYSTEM_ADMIN.equals(role)) {
            throw new BusinessRuleException(ErrorMessages.ACCESS_DENIED);
        }
    }

    private boolean hasPlatformScope(User user) {
        String roleName = user.getRole().getName();
        return RoleConstants.SUPER_ADMIN.equals(roleName) || RoleConstants.SYSTEM_ADMIN.equals(roleName);
    }

    private void ensureUserInScope(User currentUser, User targetUser) {
        if (currentUser.getState() != null && targetUser.getState() != null
                && !currentUser.getState().getId().equals(targetUser.getState().getId())) {
            throw new GeographicScopeException();
        }
    }
}
