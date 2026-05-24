package com.ask.service.impl;

import com.ask.dto.request.user.PermissionAssignmentRequest;
import com.ask.dto.request.user.UserCreateRequest;
import com.ask.dto.response.user.UserResponse;
import com.ask.entity.*;
import com.ask.exception.BusinessRuleException;
import com.ask.exception.InvalidRequestException;
import com.ask.mapper.UserMapper;
import com.ask.repository.*;
import com.ask.service.AuditService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private StateRepository stateRepository;
    @Mock
    private DistrictRepository districtRepository;
    @Mock
    private BlockRepository blockRepository;
    @Mock
    private StoreRepository storeRepository;
    @Mock
    private PermissionRepository permissionRepository;
    @Mock
    private UserPermissionRepository userPermissionRepository;
    @Mock
    private TwoFactorConfigRepository twoFactorConfigRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AuditService auditService;
    @Mock
    private com.ask.service.FileStorageService fileStorageService;
    @Mock
    private com.ask.service.NotificationService notificationService;
    @Mock
    private com.ask.util.EncryptionUtil encryptionUtil;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void createUserRejectsFullAadhaarNumberBeforeSaving() {
        User superAdmin = user(1L, "admin@askhealth.in", role(1L, "SUPER_ADMIN", 1));
        Role receptionistRole = role(6L, "RECEPTIONIST", 6);

        when(userRepository.findByEmail(superAdmin.getEmail())).thenReturn(Optional.of(superAdmin));
        when(roleRepository.findById(receptionistRole.getId())).thenReturn(Optional.of(receptionistRole));

        UserCreateRequest request = UserCreateRequest.builder()
                .fullName("New Receptionist")
                .email("new@askhealth.in")
                .temporaryPassword("TempPass@123")
                .roleId(receptionistRole.getId())
                .aadhaarLastFour("123456789012")
                .build();

        assertThrows(InvalidRequestException.class,
                () -> userService.createUser(request, superAdmin.getEmail()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUserRejectsRoleAtOrAboveCurrentUser() {
        User stateAdmin = user(2L, "state@askhealth.in", role(3L, "STATE_ADMIN", 3));
        Role systemAdminRole = role(2L, "SYSTEM_ADMIN", 2);

        when(userRepository.findByEmail(stateAdmin.getEmail())).thenReturn(Optional.of(stateAdmin));
        when(roleRepository.findById(systemAdminRole.getId())).thenReturn(Optional.of(systemAdminRole));

        UserCreateRequest request = UserCreateRequest.builder()
                .fullName("System Admin")
                .email("system@askhealth.in")
                .temporaryPassword("TempPass@123")
                .roleId(systemAdminRole.getId())
                .build();

        assertThrows(BusinessRuleException.class,
                () -> userService.createUser(request, stateAdmin.getEmail()));
        verify(userRepository, never()).save(any());
    }

    @Test
    void assignPermissionsReplacesExistingDirectPermissionsAndAudits() {
        Role superRole = role(1L, "SUPER_ADMIN", 1);
        User superAdmin = user(1L, "admin@askhealth.in", superRole);
        User targetUser = user(10L, "user@askhealth.in", role(6L, "RECEPTIONIST", 6));
        Permission permission = Permission.builder().id(99L).module("USERS").action("VIEW").build();

        when(userRepository.findByEmail(superAdmin.getEmail())).thenReturn(Optional.of(superAdmin));
        when(userRepository.findById(targetUser.getId())).thenReturn(Optional.of(targetUser));
        when(permissionRepository.findAllById(List.of(permission.getId()))).thenReturn(List.of(permission));
        when(userMapper.toUserResponse(targetUser)).thenReturn(UserResponse.builder().id(targetUser.getId()).build());
        when(userPermissionRepository.findPermissionStringsByUserId(targetUser.getId()))
                .thenReturn(List.of("USERS:VIEW"));

        userService.assignPermissions(targetUser.getId(),
                PermissionAssignmentRequest.builder().permissionIds(List.of(permission.getId())).build(),
                superAdmin.getEmail());

        verify(userPermissionRepository).deleteByUserId(targetUser.getId());
        verify(userPermissionRepository).saveAll(any());
        verify(auditService).log(eq(superAdmin), eq("ASSIGN_USER_PERMISSIONS"), eq("USER"),
                eq(targetUser.getId()), eq(null), eq(null), eq(null), any());
    }

    private User user(Long id, String email, Role role) {
        return User.builder()
                .id(id)
                .fullName("Test User")
                .email(email)
                .role(role)
                .build();
    }

    private Role role(Long id, String name, int hierarchyLevel) {
        return Role.builder()
                .id(id)
                .name(name)
                .displayName(name)
                .hierarchyLevel(hierarchyLevel)
                .build();
    }
}
