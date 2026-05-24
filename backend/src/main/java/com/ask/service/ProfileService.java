package com.ask.service;

import com.ask.dto.request.user.UserUpdateRequest;
import com.ask.dto.response.user.UserResponse;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service interface for user profile and KYC operations.
 */
public interface ProfileService {

    /**
     * Retrieves the profile of the currently logged-in user.
     *
     * @param email user email
     * @return UserResponse
     */
    UserResponse getProfile(String email);

    /**
     * Updates profile details of the currently logged-in user.
     * Note: Users can only update their contact details/basic info.
     *
     * @param request update request containing basic details
     * @param email user email
     * @return UserResponse
     */
    UserResponse updateProfile(UserUpdateRequest request, String email);

    /**
     * Uploads/updates profile photo for the logged-in user.
     *
     * @param file multipart photo file
     * @param email user email
     * @return UserResponse
     */
    UserResponse uploadPhoto(MultipartFile file, String email);

    /**
     * Submits KYC details and document for the user.
     *
     * @param aadhaarFile Aadhaar document
     * @param bankName name of the bank
     * @param bankIfsc IFSC code of the bank
     * @param bankAccount bank account number (will be encrypted)
     * @param panNumber PAN number
     * @param aadhaarLastFour last 4 digits of Aadhaar
     * @param email user email
     * @return UserResponse
     */
    UserResponse submitKyc(MultipartFile aadhaarFile, String bankName, String bankIfsc,
                           String bankAccount, String panNumber, String aadhaarLastFour, String email);

    /**
     * Downloads/loads the logged-in user's own Aadhaar document.
     *
     * @param email user email
     * @return Resource representing the file
     */
    Resource getKycDocument(String email);

    /**
     * Downloads/loads the logged-in user's profile photo.
     *
     * @param email user email
     * @return Resource representing the file
     */
    Resource getProfilePhoto(String email);
}
