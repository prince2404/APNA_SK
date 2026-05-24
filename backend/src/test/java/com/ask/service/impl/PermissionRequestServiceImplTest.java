package com.ask.service.impl;

import com.ask.dto.request.user.RequestReviewRequest;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.user.PermissionRequestResponse;
import com.ask.entity.Permission;
import com.ask.entity.PermissionRequest;
import com.ask.entity.Role;
import com.ask.entity.User;
import com.ask.enums.NotificationType;
import com.ask.enums.RequestStatus;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.DuplicateResourceException;
import com.ask.repository.PermissionRepository;
import com.ask.repository.PermissionRequestRepository;
import com.ask.repository.UserPermissionRepository;
import com.ask.repository.UserRepository;
import com.ask.service.AuditService;
import com.ask.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PermissionRequestServiceImplTest {

    @Mock
    private PermissionRequestRepository permissionRequestRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private UserPermissionRepository userPermissionRepository;
    @Mock
    private NotificationService notificationService;
    @Mock
    private AuditService auditService;

    @InjectMocks
    private PermissionRequestServiceImpl permissionRequestService;

    @Test
    void createRequestSavesRequestAndNotifiesAdmins() {
        User user = testUser(100L, "volunteer@askhealth.in", "VOLUNTEER");
        Permission permission = Permission.builder().id(5L).module("PATIENTS").action("CREATE").build();

        when(userRepository.findByEmail("volunteer@askhealth.in")).thenReturn(Optional.of(user));
        when(userRepository.findByRoleNameIn(anyList())).thenReturn(List.of(testUser(1L, "admin@askhealth.in", "SUPER_ADMIN")));
        when(permissionRepository.findById(5L)).thenReturn(Optional.of(permission));
        when(userPermissionRepository.existsByUserIdAndPermissionId(user.getId(), 5L)).thenReturn(false);
        when(permissionRequestRepository.existsByUserIdAndPermissionIdAndStatus(user.getId(), 5L, RequestStatus.PENDING)).thenReturn(false);
        when(permissionRequestRepository.save(any(PermissionRequest.class))).thenAnswer(invocation -> {
            PermissionRequest req = invocation.getArgument(0);
            req.setId(10L);
            return req;
        });

        PermissionRequestResponse result = permissionRequestService.createRequest(5L, "Need access", "volunteer@askhealth.in");

        assertNotNull(result);
        assertEquals(10L, result.getId());
        assertEquals(RequestStatus.PENDING, result.getStatus());

        verify(notificationService, atLeastOnce()).sendNotification(any(), eq(NotificationType.PERMISSION_REQUEST), anyString(), anyString(), eq("PERMISSION_REQUEST"), eq(10L));
        verify(auditService).log(eq(user), eq("CREATE_PERMISSION_REQUEST"), eq("PERMISSION_REQUEST"), eq(10L), any(), any(), any(), anyString());
    }

    @Test
    void createRequestThrowsExceptionIfAlreadyAssigned() {
        User user = testUser(100L, "volunteer@askhealth.in", "VOLUNTEER");
        Permission permission = Permission.builder().id(5L).module("PATIENTS").action("CREATE").build();

        when(userRepository.findByEmail("volunteer@askhealth.in")).thenReturn(Optional.of(user));
        when(permissionRepository.findById(5L)).thenReturn(Optional.of(permission));
        when(userPermissionRepository.existsByUserIdAndPermissionId(user.getId(), 5L)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () ->
                permissionRequestService.createRequest(5L, "Need access", "volunteer@askhealth.in")
        );
    }

    @Test
    void createRequestThrowsExceptionIfPendingRequestExists() {
        User user = testUser(100L, "volunteer@askhealth.in", "VOLUNTEER");
        Permission permission = Permission.builder().id(5L).module("PATIENTS").action("CREATE").build();

        when(userRepository.findByEmail("volunteer@askhealth.in")).thenReturn(Optional.of(user));
        when(permissionRepository.findById(5L)).thenReturn(Optional.of(permission));
        when(userPermissionRepository.existsByUserIdAndPermissionId(user.getId(), 5L)).thenReturn(false);
        when(permissionRequestRepository.existsByUserIdAndPermissionIdAndStatus(user.getId(), 5L, RequestStatus.PENDING)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                permissionRequestService.createRequest(5L, "Need access", "volunteer@askhealth.in")
        );
    }

    @Test
    void getRequestsForAdminReturnsAllRequests() {
        User admin = testUser(1L, "admin@askhealth.in", "SUPER_ADMIN");
        PermissionRequest req = PermissionRequest.builder()
                .id(1L)
                .user(testUser(2L, "user@askhealth.in", "VOLUNTEER"))
                .permission(Permission.builder().id(1L).module("M").action("A").build())
                .status(RequestStatus.PENDING)
                .build();
        Page<PermissionRequest> page = new PageImpl<>(List.of(req));

        when(userRepository.findByEmail("admin@askhealth.in")).thenReturn(Optional.of(admin));
        when(permissionRequestRepository.findAll(any(Pageable.class))).thenReturn(page);

        PageResponse<PermissionRequestResponse> result = permissionRequestService.getRequests("admin@askhealth.in", null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getRequestsForUserReturnsOnlyTheirRequests() {
        User volunteer = testUser(100L, "volunteer@askhealth.in", "VOLUNTEER");
        PermissionRequest req = PermissionRequest.builder()
                .id(1L)
                .user(volunteer)
                .permission(Permission.builder().id(1L).module("M").action("A").build())
                .status(RequestStatus.PENDING)
                .build();
        Page<PermissionRequest> page = new PageImpl<>(List.of(req));

        when(userRepository.findByEmail("volunteer@askhealth.in")).thenReturn(Optional.of(volunteer));
        when(permissionRequestRepository.findByUserId(eq(100L), any(Pageable.class))).thenReturn(page);

        PageResponse<PermissionRequestResponse> result = permissionRequestService.getRequests("volunteer@askhealth.in", null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void reviewRequestApprovesAndGrantsPermission() {
        User admin = testUser(1L, "admin@askhealth.in", "SUPER_ADMIN");
        User volunteer = testUser(100L, "volunteer@askhealth.in", "VOLUNTEER");
        Permission permission = Permission.builder().id(5L).module("PATIENTS").action("CREATE").build();
        PermissionRequest req = PermissionRequest.builder()
                .id(10L)
                .user(volunteer)
                .permission(permission)
                .status(RequestStatus.PENDING)
                .build();
        RequestReviewRequest reviewRequest = RequestReviewRequest.builder().status(RequestStatus.APPROVED).build();

        when(userRepository.findByEmail("admin@askhealth.in")).thenReturn(Optional.of(admin));
        when(permissionRequestRepository.findById(10L)).thenReturn(Optional.of(req));
        when(userPermissionRepository.existsByUserIdAndPermissionId(100L, 5L)).thenReturn(false);
        when(permissionRequestRepository.save(any(PermissionRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PermissionRequestResponse result = permissionRequestService.reviewRequest(10L, reviewRequest, "admin@askhealth.in");

        assertNotNull(result);
        assertEquals(RequestStatus.APPROVED, result.getStatus());
        verify(userPermissionRepository).save(any());
        verify(notificationService).sendNotification(eq(volunteer), eq(NotificationType.PERMISSION_REVIEW), anyString(), anyString(), eq("PERMISSION_REQUEST"), eq(10L));
        verify(auditService).log(eq(admin), eq("REVIEW_PERMISSION_REQUEST"), eq("PERMISSION_REQUEST"), eq(10L), any(), any(), any(), anyString());
    }

    @Test
    void reviewRequestRejectsAndDoesNotGrantPermission() {
        User admin = testUser(1L, "admin@askhealth.in", "SUPER_ADMIN");
        User volunteer = testUser(100L, "volunteer@askhealth.in", "VOLUNTEER");
        Permission permission = Permission.builder().id(5L).module("PATIENTS").action("CREATE").build();
        PermissionRequest req = PermissionRequest.builder()
                .id(10L)
                .user(volunteer)
                .permission(permission)
                .status(RequestStatus.PENDING)
                .build();
        RequestReviewRequest reviewRequest = RequestReviewRequest.builder().status(RequestStatus.REJECTED).build();

        when(userRepository.findByEmail("admin@askhealth.in")).thenReturn(Optional.of(admin));
        when(permissionRequestRepository.findById(10L)).thenReturn(Optional.of(req));
        when(permissionRequestRepository.save(any(PermissionRequest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PermissionRequestResponse result = permissionRequestService.reviewRequest(10L, reviewRequest, "admin@askhealth.in");

        assertNotNull(result);
        assertEquals(RequestStatus.REJECTED, result.getStatus());
        verify(userPermissionRepository, never()).save(any());
    }

    private User testUser(Long id, String email, String roleName) {
        int level = 7;
        if ("SUPER_ADMIN".equals(roleName)) level = 1;
        return User.builder()
                .id(id)
                .fullName("Test User")
                .email(email)
                .role(Role.builder().name(roleName).hierarchyLevel(level).build())
                .build();
    }
}
