package com.ask.service.impl;

import com.ask.dto.request.user.UserUpdateRequest;
import com.ask.dto.response.user.UserResponse;
import com.ask.entity.Role;
import com.ask.entity.User;
import com.ask.enums.Gender;
import com.ask.enums.NotificationType;
import com.ask.enums.VerificationStatus;
import com.ask.exception.DuplicateResourceException;
import com.ask.exception.InvalidRequestException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.mapper.UserMapper;
import com.ask.repository.UserPermissionRepository;
import com.ask.repository.UserRepository;
import com.ask.service.AuditService;
import com.ask.service.FileStorageService;
import com.ask.service.NotificationService;
import com.ask.util.EncryptionUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserPermissionRepository userPermissionRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private EncryptionUtil encryptionUtil;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private AuditService auditService;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Test
    void getProfileReturnsExpectedResponse() {
        User user = testUser("test@askhealth.in");
        UserResponse response = UserResponse.builder().email("test@askhealth.in").build();

        when(userRepository.findByEmail("test@askhealth.in")).thenReturn(Optional.of(user));
        when(userMapper.toUserResponse(user)).thenReturn(response);
        when(userPermissionRepository.findPermissionStringsByUserId(user.getId())).thenReturn(List.of("USERS:VIEW"));

        UserResponse result = profileService.getProfile("test@askhealth.in");

        assertNotNull(result);
        assertEquals("test@askhealth.in", result.getEmail());
        assertEquals(List.of("USERS:VIEW"), result.getPermissions());
    }

    @Test
    void updateProfileValidatesAndSavesUser() {
        User user = testUser("test@askhealth.in");
        UserUpdateRequest request = UserUpdateRequest.builder()
                .fullName("Updated Name")
                .email("test@askhealth.in")
                .phone("9876543210")
                .address("New Address")
                .build();

        when(userRepository.findByEmail("test@askhealth.in")).thenReturn(Optional.of(user));
        when(userRepository.existsByPhone("9876543210")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(UserResponse.builder().fullName("Updated Name").build());

        UserResponse result = profileService.updateProfile(request, "test@askhealth.in");

        assertNotNull(result);
        assertEquals("Updated Name", result.getFullName());
        verify(auditService).log(eq(user), eq("UPDATE_PROFILE"), eq("USER"), eq(user.getId()), any(), any(), any(), anyString());
    }

    @Test
    void updateProfileThrowsExceptionWhenEmailAlreadyExists() {
        User user = testUser("test@askhealth.in");
        UserUpdateRequest request = UserUpdateRequest.builder()
                .fullName("Name")
                .email("existing@askhealth.in")
                .build();

        when(userRepository.findByEmail("test@askhealth.in")).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("existing@askhealth.in")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> profileService.updateProfile(request, "test@askhealth.in"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void uploadPhotoSavesFilenameAndDeletesOldPhoto() {
        User user = testUser("test@askhealth.in");
        user.setProfilePhotoUrl("old.jpg");
        MultipartFile file = mock(MultipartFile.class);

        when(userRepository.findByEmail("test@askhealth.in")).thenReturn(Optional.of(user));
        when(fileStorageService.storeFile(file, "profile-photos")).thenReturn("new.jpg");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(UserResponse.builder().profilePhotoUrl("new.jpg").build());

        UserResponse result = profileService.uploadPhoto(file, "test@askhealth.in");

        assertNotNull(result);
        assertEquals("new.jpg", result.getProfilePhotoUrl());
        verify(fileStorageService).deleteFile("old.jpg", "profile-photos");
        verify(auditService).log(eq(user), eq("UPLOAD_PHOTO"), eq("USER"), eq(user.getId()), any(), any(), any(), anyString());
    }

    @Test
    void submitKycValidatesAadhaarLastFourAndEncryptsBankInfo() {
        User user = testUser("test@askhealth.in");
        MultipartFile file = mock(MultipartFile.class);

        when(userRepository.findByEmail("test@askhealth.in")).thenReturn(Optional.of(user));
        when(userRepository.findByRoleNameIn(anyList())).thenReturn(List.of(testUser("admin@askhealth.in")));
        when(fileStorageService.storeFile(file, "kyc-docs")).thenReturn("aadhaar_doc.pdf");
        when(encryptionUtil.encrypt("1234567890")).thenReturn("encrypted_account");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userMapper.toUserResponse(any(User.class))).thenReturn(UserResponse.builder().verificationStatus(VerificationStatus.PENDING).build());

        UserResponse result = profileService.submitKyc(file, "Test Bank", "IFSC0001234", "1234567890", "ABCDE1234F", "4321", "test@askhealth.in");

        assertNotNull(result);
        assertEquals(VerificationStatus.PENDING, result.getVerificationStatus());
        assertEquals("Test Bank", user.getBankName());
        assertEquals("IFSC0001234", user.getBankIfsc());
        assertEquals("encrypted_account", user.getBankAccountEncrypted());
        assertEquals("ABCDE1234F", user.getPanNumber());
        assertEquals("4321", user.getAadhaarLastFour());
        assertEquals("aadhaar_doc.pdf", user.getAadhaarDocUrl());

        verify(notificationService, atLeastOnce()).sendNotification(any(), eq(NotificationType.KYC_SUBMISSION), anyString(), anyString(), eq("USER"), eq(user.getId()));
        verify(auditService).log(eq(user), eq("SUBMIT_KYC"), eq("USER"), eq(user.getId()), any(), any(), any(), anyString());
    }

    @Test
    void submitKycRejects12DigitAadhaar() {
        User user = testUser("test@askhealth.in");
        MultipartFile file = mock(MultipartFile.class);
        when(userRepository.findByEmail("test@askhealth.in")).thenReturn(Optional.of(user));
        assertThrows(InvalidRequestException.class, () ->
                profileService.submitKyc(file, "Bank", "IFSC", "Acc", "PAN", "123456789012", "test@askhealth.in")
        );
    }

    @Test
    void getKycDocumentLoadsDocument() {
        User user = testUser("test@askhealth.in");
        user.setAadhaarDocUrl("aadhaar.pdf");
        Resource resource = mock(Resource.class);

        when(userRepository.findByEmail("test@askhealth.in")).thenReturn(Optional.of(user));
        when(fileStorageService.loadFileAsResource("aadhaar.pdf", "kyc-docs")).thenReturn(resource);

        Resource result = profileService.getKycDocument("test@askhealth.in");
        assertNotNull(result);
    }

    @Test
    void getKycDocumentThrowsExceptionIfDocUrlIsNull() {
        User user = testUser("test@askhealth.in");
        user.setAadhaarDocUrl(null);

        when(userRepository.findByEmail("test@askhealth.in")).thenReturn(Optional.of(user));

        assertThrows(ResourceNotFoundException.class, () -> profileService.getKycDocument("test@askhealth.in"));
    }

    private User testUser(String email) {
        return User.builder()
                .id(100L)
                .fullName("Test User")
                .email(email)
                .role(Role.builder().name("VOLUNTEER").hierarchyLevel(7).build())
                .build();
    }
}
