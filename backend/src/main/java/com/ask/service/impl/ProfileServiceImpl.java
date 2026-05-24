package com.ask.service.impl;

import com.ask.constants.ErrorMessages;
import com.ask.constants.RoleConstants;
import com.ask.dto.request.user.UserUpdateRequest;
import com.ask.dto.response.user.UserResponse;
import com.ask.entity.User;
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
import com.ask.service.ProfileService;
import com.ask.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final UserMapper userMapper;
    private final EncryptionUtil encryptionUtil;
    private final FileStorageService fileStorageService;
    private final AuditService auditService;
    private final NotificationService notificationService;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getProfile(String email) {
        User user = getUser(email);
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateProfile(UserUpdateRequest request, String email) {
        User user = getUser(email);

        String newEmail = request.getEmail().toLowerCase(Locale.ROOT);
        if (!user.getEmail().equalsIgnoreCase(newEmail)) {
            if (userRepository.existsByEmail(newEmail)) {
                throw new DuplicateResourceException(ErrorMessages.EMAIL_ALREADY_EXISTS);
            }
            user.setEmail(newEmail);
        }

        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            String newPhone = blankToNull(request.getPhone());
            if (!newPhone.equals(user.getPhone()) && userRepository.existsByPhone(newPhone)) {
                throw new DuplicateResourceException(ErrorMessages.PHONE_ALREADY_EXISTS);
            }
            user.setPhone(newPhone);
        } else {
            user.setPhone(null);
        }

        user.setFullName(request.getFullName());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setGender(request.getGender());
        user.setAddress(request.getAddress());

        User saved = userRepository.save(user);
        auditService.log(saved, "UPDATE_PROFILE", "USER", saved.getId(), null, null, null,
                "Updated profile details for: " + saved.getEmail());

        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse uploadPhoto(MultipartFile file, String email) {
        User user = getUser(email);

        if (user.getProfilePhotoUrl() != null && !user.getProfilePhotoUrl().isBlank()) {
            fileStorageService.deleteFile(user.getProfilePhotoUrl(), "profile-photos");
        }

        String filename = fileStorageService.storeFile(file, "profile-photos");
        user.setProfilePhotoUrl(filename);

        User saved = userRepository.save(user);
        auditService.log(saved, "UPLOAD_PHOTO", "USER", saved.getId(), null, null, null,
                "Uploaded profile photo: " + filename);

        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse submitKyc(MultipartFile aadhaarFile, String bankName, String bankIfsc,
                                   String bankAccount, String panNumber, String aadhaarLastFour, String email) {
        User user = getUser(email);

        validateAadhaarLastFour(aadhaarLastFour);

        if (user.getAadhaarDocUrl() != null && !user.getAadhaarDocUrl().isBlank()) {
            fileStorageService.deleteFile(user.getAadhaarDocUrl(), "kyc-docs");
        }

        String filename = fileStorageService.storeFile(aadhaarFile, "kyc-docs");

        user.setBankName(bankName);
        user.setBankIfsc(bankIfsc);
        user.setBankAccountEncrypted(encryptionUtil.encrypt(bankAccount));
        user.setPanNumber(panNumber);
        user.setAadhaarLastFour(aadhaarLastFour);
        user.setAadhaarDocUrl(filename);
        user.setVerificationStatus(VerificationStatus.PENDING);

        User saved = userRepository.save(user);

        // Notify Super Admins and System Admins
        List<User> admins = userRepository.findByRoleNameIn(List.of(RoleConstants.SUPER_ADMIN, RoleConstants.SYSTEM_ADMIN));
        for (User admin : admins) {
            try {
                notificationService.sendNotification(
                        admin,
                        NotificationType.KYC_SUBMISSION,
                        "New KYC Submission",
                        "User " + user.getFullName() + " (" + user.getEmail() + ") has submitted KYC details for verification.",
                        "USER",
                        user.getId()
                );
            } catch (Exception e) {
                log.error("Failed to send KYC submission notification to admin: {}", admin.getEmail(), e);
            }
        }

        auditService.log(user, "SUBMIT_KYC", "USER", user.getId(), null, null, null,
                "Submitted KYC details and document: " + filename);

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getKycDocument(String email) {
        User user = getUser(email);
        if (user.getAadhaarDocUrl() == null || user.getAadhaarDocUrl().isBlank()) {
            throw new ResourceNotFoundException("Aadhaar document", "email", email);
        }
        return fileStorageService.loadFileAsResource(user.getAadhaarDocUrl(), "kyc-docs");
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getProfilePhoto(String email) {
        User user = getUser(email);
        if (user.getProfilePhotoUrl() == null || user.getProfilePhotoUrl().isBlank()) {
            throw new ResourceNotFoundException("Profile photo", "email", email);
        }
        return fileStorageService.loadFileAsResource(user.getProfilePhotoUrl(), "profile-photos");
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private UserResponse toResponse(User user) {
        UserResponse response = userMapper.toUserResponse(user);
        response.setPermissions(userPermissionRepository.findPermissionStringsByUserId(user.getId()));
        if (user.getBankAccountEncrypted() != null && !user.getBankAccountEncrypted().isBlank()) {
            try {
                response.setBankAccount(encryptionUtil.decrypt(user.getBankAccountEncrypted()));
            } catch (Exception e) {
                log.error("Failed to decrypt bank account for user: {}", user.getEmail(), e);
            }
        }
        return response;
    }

    private void validateAadhaarLastFour(String aadhaarLastFour) {
        if (aadhaarLastFour == null || aadhaarLastFour.isBlank()) {
            return;
        }
        if (aadhaarLastFour.matches("\\d{12}")) {
            throw new InvalidRequestException(ErrorMessages.AADHAAR_FULL_NUMBER_REJECTED);
        }
        if (!aadhaarLastFour.matches("\\d{4}")) {
            throw new InvalidRequestException("Aadhaar last four must contain exactly 4 digits");
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
