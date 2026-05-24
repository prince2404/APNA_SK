package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.request.patient.PatientRequest;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.patient.PatientResponse;
import com.ask.service.PatientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import jakarta.validation.Valid;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping(ApiPaths.PATIENTS)
@RequiredArgsConstructor
public class PatientController {

    private final PatientService patientService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN', 'DISTRICT_ADMIN', 'BLOCK_ADMIN', 'RECEPTIONIST', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<PatientResponse>> registerPatient(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PatientRequest request) {
        PatientResponse response = patientService.registerPatient(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Patient registered successfully", ApiPaths.PATIENTS));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN', 'DISTRICT_ADMIN', 'BLOCK_ADMIN', 'RECEPTIONIST', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody PatientRequest request) {
        PatientResponse response = patientService.updatePatient(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Patient updated successfully", ApiPaths.PATIENTS + "/" + id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN', 'DISTRICT_ADMIN', 'BLOCK_ADMIN', 'RECEPTIONIST', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<PageResponse<PatientResponse>>> getPatients(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long storeId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<PatientResponse> response = patientService.getPatients(search, storeId, page, size, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.PATIENTS));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN', 'DISTRICT_ADMIN', 'BLOCK_ADMIN', 'RECEPTIONIST', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        PatientResponse response = patientService.getPatientById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.PATIENTS + "/" + id));
    }

    @PostMapping(value = ApiPaths.PATIENTS_BULK_UPLOAD, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN', 'DISTRICT_ADMIN', 'BLOCK_ADMIN', 'RECEPTIONIST', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> bulkUploadPatients(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("file") MultipartFile file) {
        Map<String, Object> response = patientService.bulkUploadPatients(file, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Bulk upload completed", ApiPaths.PATIENTS + ApiPaths.PATIENTS_BULK_UPLOAD));
    }
}
