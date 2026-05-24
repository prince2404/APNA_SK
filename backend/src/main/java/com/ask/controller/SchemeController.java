package com.ask.controller;

import com.ask.constants.ApiPaths;
import com.ask.dto.request.billing.SchemeRequest;
import com.ask.dto.response.common.ApiResponse;
import com.ask.dto.response.billing.SchemeResponse;
import com.ask.service.SchemeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(ApiPaths.SCHEMES)
@RequiredArgsConstructor
public class SchemeController {

    private final SchemeService schemeService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN')")
    public ResponseEntity<ApiResponse<SchemeResponse>> createScheme(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SchemeRequest request) {
        SchemeResponse response = schemeService.createScheme(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Scheme created successfully", ApiPaths.SCHEMES));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<SchemeResponse>>> getSchemes(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<SchemeResponse> response = schemeService.getSchemes(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, ApiPaths.SCHEMES));
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'SYSTEM_ADMIN', 'STATE_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> toggleSchemeStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        schemeService.toggleSchemeStatus(id, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(null, "Scheme status toggled successfully", ApiPaths.SCHEMES + "/" + id + "/toggle-status"));
    }
}
