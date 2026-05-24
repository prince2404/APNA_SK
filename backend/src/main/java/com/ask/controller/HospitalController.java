package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.request.hospital.HospitalRequest;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.common.PageResponse;
import com.ask.dto.response.hospital.HospitalResponse;
import com.ask.service.HospitalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping(ApiPaths.HOSPITALS)
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN')")
    public ResponseEntity<ApiResponse<HospitalResponse>> createHospital(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody HospitalRequest request) {
        HospitalResponse response = hospitalService.createHospital(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Hospital created successfully", ApiPaths.HOSPITALS));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN')")
    public ResponseEntity<ApiResponse<HospitalResponse>> updateHospital(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @RequestBody HospitalRequest request) {
        HospitalResponse response = hospitalService.updateHospital(id, request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Hospital updated successfully", ApiPaths.HOSPITALS + "/" + id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN', 'DISTRICT_ADMIN', 'BLOCK_ADMIN', 'RECEPTIONIST', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<PageResponse<HospitalResponse>>> getHospitals(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long stateId,
            @RequestParam(required = false) Long districtId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageResponse<HospitalResponse> response = hospitalService.getHospitals(stateId, districtId, page, size, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.HOSPITALS));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN', 'DISTRICT_ADMIN', 'BLOCK_ADMIN', 'RECEPTIONIST', 'VOLUNTEER')")
    public ResponseEntity<ApiResponse<HospitalResponse>> getHospitalById(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        HospitalResponse response = hospitalService.getHospitalById(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.HOSPITALS + "/" + id));
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleHospitalStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        hospitalService.toggleHospitalStatus(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Hospital status toggled successfully", ApiPaths.HOSPITALS + "/" + id + "/toggle-status"));
    }
}
