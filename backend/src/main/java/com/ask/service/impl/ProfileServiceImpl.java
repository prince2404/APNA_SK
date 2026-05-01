package com.ask.service.impl;

import com.ask.constants.AppConstants;
import com.ask.constants.ErrorMessages;
import com.ask.dto.request.profile.BankDetailsRequest;
import com.ask.dto.request.profile.ProfileUpdateRequest;
import com.ask.dto.response.user.UserResponse;
import com.ask.entity.User;
import com.ask.enums.VerificationStatus;
import com.ask.exception.InvalidRequestException;
import com.ask.exception.ResourceNotFoundException;
import com.ask.mapper.UserMapper;
import com.ask.repository.UserPermissionRepository;
import com.ask.repository.UserRepository;
import com.ask.service.AuditService;
import com.ask.service.ProfileService;
import com.ask.util.EncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

/**
 * Profile management implementation for self-service profile updates.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private static final String UPLOAD_DIR = "uploads";

    private final UserRepository userRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final UserMapper userMapper;
    private final AuditService auditService;
    private final EncryptionUtil encryptionUtil;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getMyProfile(String currentUserEmail) {
        User user = getUser(currentUserEmail);
        return toResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateMyProfile(ProfileUpdateRequest request, String currentUserEmail) {
        User user = getUser(currentUserEmail);

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            user.setPhone(blankToNull(request.getPhone()));
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        if (request.getGender() != null) {
            user.setGender(request.getGender());
        }
        if (request.getAddress() != null) {
            user.setAddress(request.getAddress());
        }
        if (request.getAadhaarLastFour() != null) {
            validateAadhaarLastFour(request.getAadhaarLastFour());
            user.setAadhaarLastFour(blankToNull(request.getAadhaarLastFour()));
        }
        if (request.getPanNumber() != null) {
            user.setPanNumber(blankToNull(request.getPanNumber()));
        }

        User saved = userRepository.save(user);
        auditService.log(saved, "UPDATE_PROFILE", "USER", saved.getId(), null, null, null,
                "User updated own profile");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse uploadProfilePhoto(MultipartFile file, String currentUserEmail) {
        validateImageFile(file);
        User user = getUser(currentUserEmail);

        String fileName = storeFile(file, "photos");
        user.setProfilePhotoUrl(fileName);

        User saved = userRepository.save(user);
        auditService.log(saved, "UPLOAD_PROFILE_PHOTO", "USER", saved.getId(), null, null, null,
                "User uploaded profile photo");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse uploadKycDocument(MultipartFile file, String currentUserEmail) {
        validateDocumentFile(file);
        User user = getUser(currentUserEmail);

        String fileName = storeFile(file, "kyc");
        user.setAadhaarDocUrl(fileName);
        // Reset to PENDING so admins review the new document
        user.setVerificationStatus(VerificationStatus.PENDING);

        User saved = userRepository.save(user);
        auditService.log(saved, "UPLOAD_KYC_DOCUMENT", "USER", saved.getId(), null, null, null,
                "User uploaded KYC document — verification reset to PENDING");
        return toResponse(saved);
    }

    @Override
    @Transactional
    public UserResponse updateBankDetails(BankDetailsRequest request, String currentUserEmail) {
        User user = getUser(currentUserEmail);

        String encryptedAccount = encryptionUtil.encrypt(request.getAccountNumber());
        user.setBankAccountEncrypted(encryptedAccount);
        user.setBankIfsc(request.getIfscCode().toUpperCase(Locale.ROOT));
        user.setBankName(request.getBankName());

        User saved = userRepository.save(user);
        auditService.log(saved, "UPDATE_BANK_DETAILS", "USER", saved.getId(), null, null, null,
                "User updated bank details");
        return toResponse(saved);
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", email));
    }

    private UserResponse toResponse(User user) {
        UserResponse response = userMapper.toUserResponse(user);
        response.setPermissions(userPermissionRepository.findPermissionStringsByUserId(user.getId()));
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

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("File is required");
        }
        if (file.getSize() > AppConstants.MAX_FILE_SIZE_BYTES) {
            throw new InvalidRequestException("File size cannot exceed 5 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || Arrays.stream(AppConstants.ALLOWED_IMAGE_TYPES)
                .noneMatch(contentType::equalsIgnoreCase)) {
            throw new InvalidRequestException("Only JPEG, PNG, and WebP images are allowed");
        }
    }

    private void validateDocumentFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidRequestException("File is required");
        }
        if (file.getSize() > AppConstants.MAX_FILE_SIZE_BYTES) {
            throw new InvalidRequestException("File size cannot exceed 5 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || Arrays.stream(AppConstants.ALLOWED_DOCUMENT_TYPES)
                .noneMatch(contentType::equalsIgnoreCase)) {
            throw new InvalidRequestException("Only PDF, JPEG, and PNG documents are allowed");
        }
    }

    /**
     * Stores an uploaded file on the local filesystem and returns the relative path.
     * In production this should be replaced with cloud storage (S3, GCS, etc.).
     */
    private String storeFile(MultipartFile file, String subDir) {
        try {
            String originalName = StringUtils.cleanPath(
                    file.getOriginalFilename() != null ? file.getOriginalFilename() : "file");
            String extension = "";
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex >= 0) {
                extension = originalName.substring(dotIndex);
            }
            String storedName = UUID.randomUUID() + extension;
            Path uploadPath = Paths.get(UPLOAD_DIR, subDir);
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(storedName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return subDir + "/" + storedName;
        } catch (IOException e) {
            log.error("Failed to store file: {}", e.getMessage());
            throw new RuntimeException("Failed to store uploaded file", e);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }
}
