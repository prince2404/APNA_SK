package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.request.user.UserUpdateRequest;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.user.UserResponse;
import com.ask.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(ApiPaths.PROFILE)
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse>> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        UserResponse response = profileService.getProfile(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.PROFILE));
    }

    @PutMapping
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UserUpdateRequest request) {
        UserResponse response = profileService.updateProfile(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully", ApiPaths.PROFILE));
    }

    @PostMapping(value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> uploadPhoto(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        UserResponse response = profileService.uploadPhoto(file, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Profile photo uploaded successfully", ApiPaths.PROFILE + "/photo"));
    }

    @GetMapping("/photo")
    public ResponseEntity<Resource> downloadPhoto(
            @AuthenticationPrincipal UserDetails userDetails) {
        Resource resource = profileService.getProfilePhoto(userDetails.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(resource);
    }

    @PostMapping(value = "/kyc", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<UserResponse>> submitKyc(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("aadhaarFile") MultipartFile aadhaarFile,
            @RequestParam("bankName") String bankName,
            @RequestParam("bankIfsc") String bankIfsc,
            @RequestParam("bankAccount") String bankAccount,
            @RequestParam("panNumber") String panNumber,
            @RequestParam("aadhaarLastFour") String aadhaarLastFour) {
        UserResponse response = profileService.submitKyc(
                aadhaarFile, bankName, bankIfsc, bankAccount, panNumber, aadhaarLastFour, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "KYC details submitted successfully", ApiPaths.PROFILE + "/kyc"));
    }

    @GetMapping("/kyc/document")
    public ResponseEntity<Resource> downloadKycDocument(
            @AuthenticationPrincipal UserDetails userDetails) {
        Resource resource = profileService.getKycDocument(userDetails.getUsername());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
